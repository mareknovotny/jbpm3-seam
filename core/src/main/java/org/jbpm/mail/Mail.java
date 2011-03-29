package org.jbpm.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import org.jbpm.JbpmConfiguration.Configs;
import org.jbpm.JbpmException;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.el.ELException;
import org.jbpm.jpdl.el.VariableResolver;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.util.ClassLoaderUtil;
import org.jbpm.util.XmlUtil;

public class Mail implements ActionHandler {

  private String template;
  private String to;
  private String actors;
  private String cc;
  private String ccActors;
  private String bcc;
  private String bccActors;
  private String subject;
  private String text;

  private transient ExecutionContext executionContext;

  private static final long serialVersionUID = 2L;

  public Mail() {
  }

  public Mail(String template, String actors, String to, String subject, String text) {
    this.template = template;
    this.actors = actors;
    this.to = to;
    this.subject = subject;
    this.text = text;
  }

  public Mail(String template, String actors, String to, String bccActors, String bcc,
    String subject, String text) {
    this.template = template;
    this.actors = actors;
    this.to = to;
    this.bccActors = bccActors;
    this.bcc = bcc;
    this.subject = subject;
    this.text = text;
  }

  public void execute(ExecutionContext executionContext) {
    this.executionContext = executionContext;
    send();
  }

  public Collection getRecipients() {
    return collectRecipients(actors, to);
  }

  public Collection getCcRecipients() {
    return collectRecipients(ccActors, cc);
  }

  public Collection getBccRecipients() {
    Collection recipients = collectRecipients(bccActors, bcc);
    if (Configs.hasObject("jbpm.mail.bcc.address")) {
      if (!(recipients instanceof ArrayList)) recipients = new ArrayList(recipients);
      recipients.addAll(tokenize(Configs.getString("jbpm.mail.bcc.address")));
    }
    return recipients;
  }

  private Collection collectRecipients(String actors, String addresses) {
    if (actors != null) {
      if (addresses != null) {
        Collection recipients = new ArrayList(evaluateActors(actors));
        recipients.addAll(evaluateAddresses(addresses));
        return recipients;
      }
      return evaluateActors(actors);
    }
    else if (addresses != null) {
      return evaluateAddresses(addresses);
    }
    return null;
  }

  private Collection evaluateActors(String expression) {
    Object value = evaluate(expression, Object.class);
    Collection actorIds;
    if (value instanceof String) {
      actorIds = tokenize((String) value);
    }
    else if (value instanceof Collection) {
      actorIds = (Collection) value;
    }
    else if (value instanceof String[]) {
      actorIds = Arrays.asList((String[]) value);
    }
    else {
      throw new JbpmException(expression + " returned " + value
        + " instead of comma-separated string, string array or collection");
    }
    return resolveAddresses(actorIds);
  }

  protected Collection resolveAddresses(Collection actorIds) {
    AddressResolver addressResolver = (AddressResolver) Configs.getObject("jbpm.mail.address.resolver");

    Collection addresses = new ArrayList();
    for (Iterator iter = actorIds.iterator(); iter.hasNext();) {
      String actorId = (String) iter.next();
      Object result = addressResolver.resolveAddress(actorId);

      if (result instanceof String) {
        addresses.add(result);
      }
      else if (result instanceof Collection) {
        addresses.addAll((Collection) result);
      }
      else if (result instanceof String[]) {
        addresses.addAll(Arrays.asList((String[]) result));
      }
      else if (result == null) {
        // no such actor or actor has no address
      }
      else {
        throw new JbpmException(addressResolver + " returned " + result
          + " instead of single string, string array or collection");
      }
    }
    return addresses;
  }

  private Collection evaluateAddresses(String expression) {
    Object value = evaluate(expression, Object.class);
    if (value instanceof String) return tokenize((String) value);
    if (value instanceof Collection) return (Collection) value;
    if (value instanceof String[]) return Arrays.asList((String[]) value);
    // give up
    throw new JbpmException(expression + " returned " + value
      + " instead of comma-separated string, string array or collection");
  }

  protected List tokenize(String text) {
    return text != null ? Arrays.asList(text.split("[,;:]+")) : null;
  }

  private Object evaluate(String expression, Class expectedType) {
    VariableResolver variableResolver = new MailVariableResolver(getTemplateVariables(), JbpmExpressionEvaluator.getVariableResolver());
    return JbpmExpressionEvaluator.evaluate(expression, executionContext, expectedType, variableResolver, JbpmExpressionEvaluator.getFunctionMapper());
  }

  public String getSubject() {
    return subject != null ? (String) evaluate(subject, String.class) : null;
  }

  public String getText() {
    return text != null ? (String) evaluate(text, String.class) : null;
  }

  public String getFromAddress() {
    return Configs.hasObject("jbpm.mail.from.address") ?
      Configs.getString("jbpm.mail.from.address") : null;
  }

  public void send() {
    if (template != null) {
      Properties templateProperties = getTemplateProperties(template);

      if (actors == null) actors = templateProperties.getProperty("actors");
      if (to == null) to = templateProperties.getProperty("to");
      if (cc == null) cc = templateProperties.getProperty("cc");
      if (ccActors == null) ccActors = templateProperties.getProperty("cc-actors");
      if (bcc == null) bcc = templateProperties.getProperty("bcc");
      if (bccActors == null) bccActors = templateProperties.getProperty("bcc-actors");
      if (subject == null) subject = templateProperties.getProperty("subject");
      if (text == null) text = templateProperties.getProperty("text");
    }

    String sender = getFromAddress();
    Collection recipients = getRecipients();
    Collection ccRecipients = getCcRecipients();
    Collection bccRecipients = getBccRecipients();
    if (nullOrEmpty(recipients) && nullOrEmpty(ccRecipients) && nullOrEmpty(bccRecipients))
      return;

    String subject = getSubject();
    String text = getText();

    if (log.isDebugEnabled()) {
      StringBuffer detail = new StringBuffer("sending email");
      if (!nullOrEmpty(recipients)) detail.append(" to ").append(recipients);
      if (!nullOrEmpty(ccRecipients)) detail.append(" cc ").append(ccRecipients);
      if (!nullOrEmpty(bccRecipients)) detail.append(" bcc ").append(bccRecipients);
      if (subject != null) detail.append(" about '").append(subject).append('\'');
      log.debug(detail.toString());
    }

    Session session = Session.getInstance(getServerProperties());
    for (int retries = 4; retries >= 0; retries--) {
      try {
        sendInternal(session, sender, recipients, ccRecipients, bccRecipients, subject, text);
        break;
      }
      catch (MessagingException me) {
        if (retries == 0) throw new JbpmException("failed to send email", me);
        log.warn("failed to send email (" + retries + " retries left): " + me.getMessage());
      }
    }
  }

  public static void send(Properties serverProperties, String sender, Collection recipients,
    String subject, String text) {
    send(serverProperties, sender, recipients, null, subject, text);
  }

  public static void send(Properties serverProperties, String sender, Collection recipients,
    Collection bccRecipients, String subject, String text) {
    if (nullOrEmpty(recipients) && nullOrEmpty(bccRecipients)) return;

    if (log.isDebugEnabled()) {
      StringBuffer detail = new StringBuffer("sending email to ");
      detail.append(recipients);
      if (bccRecipients != null) detail.append(" bcc ").append(bccRecipients);
      if (subject != null) detail.append(" about '").append(subject).append('\'');
      log.debug(detail.toString());
    }

    Session session = Session.getInstance(serverProperties);
    for (int retries = 4; retries >= 0; retries--) {
      try {
        sendInternal(session, null, recipients, null, bccRecipients, subject, text);
        break;
      }
      catch (MessagingException me) {
        if (retries == 0) throw new JbpmException("failed to send email", me);
        log.warn("failed to send email (" + retries + " retries left): " + me.getMessage());
      }
    }
  }

  private static boolean nullOrEmpty(Collection col) {
    return col == null || col.isEmpty();
  }

  private static void sendInternal(Session session, String sender, Collection recipients,
    Collection ccRecipients, Collection bccRecipients, String subject, String text)
    throws MessagingException {
    MimeMessage message = new MimeMessage(session);
    // from
    if (sender != null) {
      message.setFrom(new InternetAddress(sender));
    }
    else {
      // read sender from session property "mail.from"
      message.setFrom();
    }
    // to
    if (recipients != null) {
      for (Iterator iter = recipients.iterator(); iter.hasNext();) {
        InternetAddress recipient = new InternetAddress((String) iter.next());
        message.addRecipient(Message.RecipientType.TO, recipient);
      }
    }
    // cc
    if (ccRecipients != null) {
      for (Iterator iter = ccRecipients.iterator(); iter.hasNext();) {
        InternetAddress recipient = new InternetAddress((String) iter.next());
        message.addRecipient(Message.RecipientType.CC, recipient);
      }
    }
    // bcc
    if (bccRecipients != null) {
      for (Iterator iter = bccRecipients.iterator(); iter.hasNext();) {
        InternetAddress recipient = new InternetAddress((String) iter.next());
        message.addRecipient(Message.RecipientType.BCC, recipient);
      }
    }
    // subject
    if (subject != null) message.setSubject(subject);
    // text
    if (text != null) message.setText(text);
    // send the message
    Transport.send(message);
  }

  private static final Map serverPropertiesByResource = new HashMap();

  private Properties getServerProperties() {
    Properties serverProperties;

    if (Configs.hasObject("resource.mail.properties")) {
      String resource = Configs.getString("resource.mail.properties");
      synchronized (serverPropertiesByResource) {
        // look in server properties cache
        serverProperties = (Properties) serverPropertiesByResource.get(resource);
        if (serverProperties == null) {
          // load server properties and put them in the cache
          serverProperties = ClassLoaderUtil.getProperties(resource);
          serverPropertiesByResource.put(resource, serverProperties);
        }
      }
    }
    else {
      serverProperties = new Properties();
      // host
      if (Configs.hasObject("jbpm.mail.smtp.host")) {
        String smtpHost = Configs.getString("jbpm.mail.smtp.host");
        serverProperties.setProperty("mail.smtp.host", smtpHost);
      }
      // port
      if (Configs.hasObject("jbpm.mail.smtp.port")) {
        int port = Configs.getInt("jbpm.mail.smtp.port");
        serverProperties.setProperty("mail.smtp.port", Integer.toString(port));
      }
    }
    return serverProperties;
  }

  private static final Map templatePropertiesByResource = new HashMap();
  private static final Map templateVariablesByResource = new HashMap();

  private static Properties getTemplateProperties(String templateName) {
    String resource = Configs.getString("resource.mail.templates");
    synchronized (templatePropertiesByResource) {
      Map templateProperties = (Map) templatePropertiesByResource.get(resource);
      if (templateProperties == null) {
        loadTemplates(resource);
        templateProperties = (Map) templatePropertiesByResource.get(resource);
      }
      return (Properties) templateProperties.get(templateName);
    }
  }

  private static Map getTemplateVariables() {
    String resource = Configs.getString("resource.mail.templates");
    synchronized (templateVariablesByResource) {
      Map templateVariables = (Map) templateVariablesByResource.get(resource);
      if (templateVariables == null) {
        loadTemplates(resource);
        templateVariables = (Map) templateVariablesByResource.get(resource);
      }
      return templateVariables;
    }
  }

  private static void loadTemplates(String resource) {
    Element templatesElement = XmlUtil.parseXmlResource(resource, true).getDocumentElement();

    Map templatePropertiesMap = new HashMap();
    for (Iterator iter = XmlUtil.elementIterator(templatesElement, "mail-template"); iter.hasNext();) {
      Element templateElement = (Element) iter.next();

      Properties templateProperties = new Properties();
      addTemplateProperty(templateElement, "to", templateProperties);
      addTemplateProperty(templateElement, "actors", templateProperties);
      addTemplateProperty(templateElement, "subject", templateProperties);
      addTemplateProperty(templateElement, "text", templateProperties);
      addTemplateProperty(templateElement, "cc", templateProperties);
      addTemplateProperty(templateElement, "cc-actors", templateProperties);
      addTemplateProperty(templateElement, "bcc", templateProperties);
      // preserve backwards compatibility with bccActors element
      Element bccActorsElement = XmlUtil.element(templateElement, "bccActors");
      if (bccActorsElement != null) {
        templateProperties.setProperty("bcc-actors", XmlUtil.getContentText(bccActorsElement));
      }
      else {
        addTemplateProperty(templateElement, "bcc-actors", templateProperties);
      }

      templatePropertiesMap.put(templateElement.getAttribute("name"), templateProperties);
    }
    templatePropertiesByResource.put(resource, templatePropertiesMap);

    Map templateVariables = new HashMap();
    for (Iterator iter = XmlUtil.elementIterator(templatesElement, "variable"); iter.hasNext();) {
      Element variableElement = (Element) iter.next();
      templateVariables.put(variableElement.getAttribute("name"), variableElement.getAttribute("value"));
    }
    templateVariablesByResource.put(resource, templateVariables);
  }

  private static void addTemplateProperty(Element templateElement, String property,
    Properties templateProperties) {
    Element element = XmlUtil.element(templateElement, property);
    if (element != null) {
      templateProperties.setProperty(property, XmlUtil.getContentText(element));
    }
  }

  static class MailVariableResolver implements VariableResolver, Serializable {

    private Map templateVariables;
    private VariableResolver variableResolver;

    private static final long serialVersionUID = 1L;

    MailVariableResolver(Map templateVariables, VariableResolver variableResolver) {
      this.templateVariables = templateVariables;
      this.variableResolver = variableResolver;
    }

    public Object resolveVariable(String pName) throws ELException {
      if (templateVariables != null && templateVariables.containsKey(pName)) {
        return templateVariables.get(pName);
      }
      return variableResolver != null ? variableResolver.resolveVariable(pName) : null;
    }
  }

  private static final Log log = LogFactory.getLog(Mail.class);
}
