package org.jbpm.mail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.Test;

import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import org.jbpm.AbstractJbpmTestCase;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.def.Swimlane;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;

public class MailTest extends AbstractJbpmTestCase {

  private JbpmContext jbpmContext;

  private static final String XML_DECL = "<?xml version='1.0'?>";

  private static JbpmConfiguration jbpmConfiguration = JbpmConfiguration.parseXmlString(XML_DECL
    + "<jbpm-configuration>"
    + "  <jbpm-context />"
    + "  <string name='resource.mail.properties' value='org/jbpm/mail/test.mail.properties' />"
    + "  <string name='jbpm.mail.from.address' value='workflow@redhat.com' /> "
    + "  <bean name='jbpm.mail.address.resolver' class='"
    + TestAddressResolver.class.getName()
    + "' singleton='true' />"
    + "</jbpm-configuration>");

  private static Wiser wiser;

  protected void setUp() throws Exception {
    super.setUp();
    jbpmContext = jbpmConfiguration.createJbpmContext();
  }

  protected void tearDown() throws Exception {
    wiser.getMessages().clear();
    jbpmContext.close();
    super.tearDown();
  }

  public static Test suite() {
    MailTestSetup testSetup = new MailTestSetup(MailTest.class);
    wiser = testSetup.getWiser();
    return testSetup;
  }

  public void testWithoutAddressResolving() throws MessagingException, IOException {
    String to = "sample.shipper@example.domain";
    String subject = "latest news";
    String text = "roy is assurancetourix";

    Mail mail = new Mail(null, null, to, subject, text);
    mail.send();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("latest news", email.getSubject());
    assertEquals("roy is assurancetourix", email.getContent());
    assert Arrays.equals(InternetAddress.parse("sample.shipper@example.domain"), email.getRecipients(RecipientType.TO));
  }

  public void testMailWithAddressResolving() throws MessagingException, IOException {
    String actors = "manager";
    String subject = "latest news";
    String text = "roy is assurancetourix";

    Mail mail = new Mail(null, actors, null, subject, text);
    mail.send();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("latest news", email.getSubject());
    assertEquals("roy is assurancetourix", email.getContent());
    assert Arrays.equals(InternetAddress.parse("manager@example.domain"), email.getRecipients(RecipientType.TO));
  }

  public void testMailWithBccAddress() throws MessagingException, IOException {
    String bcc = "bcc@example.domain";
    String subject = "latest news";
    String text = "roy is assurancetourix";

    Mail mail = new Mail(null, null, null, null, bcc, subject, text);
    mail.send();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("latest news", email.getSubject());
    assertEquals("roy is assurancetourix", email.getContent());
    assertNull(email.getRecipients(RecipientType.TO));
  }

  public void testMailNodeAttributes() throws MessagingException, IOException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='send email' />"
      + "  </start-state>"
      + "  <mail-node name='send email' actors='george' subject='readmylips' text='nomoretaxes'>"
      + "    <transition to='end' />"
      + "  </mail-node>"
      + "  <end-state name='end' />"
      + "</process-definition>");
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("readmylips", email.getSubject());
    assertEquals("nomoretaxes", email.getContent());
    assert Arrays.equals(InternetAddress.parse("george@example.domain"), email.getRecipients(RecipientType.TO));
  }

  public void testMailNodeElements() throws MessagingException, IOException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='send email' />"
      + "  </start-state>"
      + "  <mail-node name='send email' actors='george'>"
      + "    <subject>readmylips</subject>"
      + "    <text>nomoretaxes</text>"
      + "    <transition to='end' />"
      + "  </mail-node>"
      + "  <end-state name='end' />"
      + "</process-definition>");
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("readmylips", email.getSubject());
    assertEquals("nomoretaxes", email.getContent());
    assert Arrays.equals(InternetAddress.parse("george@example.domain"), email.getRecipients(RecipientType.TO));
  }

  public void testMailActionAttributes() throws MessagingException, IOException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='end'>"
      + "      <mail name='send email' actors='george' subject='readmylips' text='nomoretaxes' />"
      + "    </transition>"
      + "  </start-state>"
      + "  <end-state name='end' />"
      + "</process-definition>");
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("readmylips", email.getSubject());
    assertEquals("nomoretaxes", email.getContent());
    assert Arrays.equals(InternetAddress.parse("george@example.domain"), email.getRecipients(RecipientType.TO));
  }

  public void testMailActionElements() throws MessagingException, IOException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='end'>"
      + "      <mail actors='george'>"
      + "        <subject>readmylips</subject>"
      + "        <text>nomoretaxes</text>"
      + "      </mail>"
      + "    <transition to='end' />"
      + "    </transition>"
      + "  </start-state>"
      + "  <end-state name='end' />"
      + "</process-definition>");
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("readmylips", email.getSubject());
    assertEquals("nomoretaxes", email.getContent());
    assert Arrays.equals(InternetAddress.parse("george@example.domain"), email.getRecipients(RecipientType.TO));
  }

  public void testMultipleRecipients() throws MessagingException, IOException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='end'>"
      + "      <mail name='send email' actors='george; barbara; suzy'"
      + " subject='readmylips' text='nomoretaxes' />"
      + "    </transition>"
      + "  </start-state>"
      + "  <end-state name='end' />"
      + "</process-definition>");
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(3, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("readmylips", email.getSubject());
    assertEquals("nomoretaxes", email.getContent());
    InternetAddress[] expectedTo = InternetAddress.parse("george@example.domain, barbara@example.domain, suzy@example.domain");
    assert Arrays.equals(expectedTo, email.getRecipients(RecipientType.TO));
  }

  public void testMailWithoutAddressResolving() throws MessagingException, IOException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='end'>"
      + "      <mail name='send email' to='george@humpydumpy.gov: spiderman@hollywood.ca.us'"
      + " subject='readmylips' text='nomoretaxes' />"
      + "    </transition>"
      + "  </start-state>"
      + "  <end-state name='end' />"
      + "</process-definition>");
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(2, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("readmylips", email.getSubject());
    assertEquals("nomoretaxes", email.getContent());
    InternetAddress[] expectedTo = InternetAddress.parse("george@humpydumpy.gov, spiderman@hollywood.ca.us");
    assert Arrays.equals(expectedTo, email.getRecipients(RecipientType.TO));
  }

  public void testToVariableExpression() throws MessagingException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='end'>"
      + "      <mail name='send email' to='#{user.email}' subject='s' text='t' />"
      + "    </transition>"
      + "  </start-state>"
      + "  <end-state name='end' />"
      + "</process-definition>");

    User mrNobody = new User("hucklebuck@example.domain");

    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.getContextInstance().setVariable("user", mrNobody);
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assert Arrays.equals(InternetAddress.parse("hucklebuck@example.domain"), email.getRecipients(RecipientType.TO));
  }

  public void testToSwimlaneExpression() throws MessagingException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='end'>"
      + "      <mail name='send email' actors='#{initiator}' subject='s' text='t' />"
      + "    </transition>"
      + "  </start-state>"
      + "  <end-state name='end' />"
      + "</process-definition>");

    SwimlaneInstance initiatorInstance = new SwimlaneInstance(new Swimlane("initiator"));
    initiatorInstance.setActorId("huckelberry");

    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.getTaskMgmtInstance().addSwimlaneInstance(initiatorInstance);
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assert Arrays.equals(InternetAddress.parse("huckelberry@example.domain"), email.getRecipients(RecipientType.TO));
  }

  public void testSubjectExpression() throws MessagingException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='end'>"
      + "      <mail name='send email' actors='me' subject='your ${item} order' text='t' />"
      + "    </transition>"
      + "  </start-state>"
      + "  <end-state name='end' />"
      + "</process-definition>");

    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.getContextInstance().setVariable("item", "cookies");
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("your cookies order", email.getSubject());
  }

  public void testTextExpression() throws IOException, MessagingException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='end'>"
      + "      <mail name='send email' actors='me' text='your ${item} order' />"
      + "    </transition>"
      + "  </start-state>"
      + "  <end-state name='end' />"
      + "</process-definition>");

    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.getContextInstance().setVariable("item", "cookies");
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();
    assertEquals("your cookies order", email.getContent());
  }

  public void testFrom() throws MessagingException {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(XML_DECL
      + "<process-definition>"
      + "  <start-state>"
      + "    <transition to='end'>"
      + "      <mail name='send email' to='long.cat@meme.org'"
      + " subject='important info' text='longcat iz looooooong' />"
      + "    </transition>"
      + "  </start-state>"
      + "  <end-state name='end' />"
      + "</process-definition>");
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();

    List messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = (WiserMessage) messages.get(0);
    MimeMessage email = message.getMimeMessage();

    Address[] from = email.getFrom();
    assertEquals(1, from.length);
    InternetAddress fromAddress = (InternetAddress) from[0];
    assertEquals("workflow@redhat.com", fromAddress.getAddress());
  }

  public static class User {
    String email;

    public User(String email) {
      this.email = email;
    }

    public String getEmail() {
      return email;
    }
  }

  public static class TestAddressResolver implements AddressResolver {
    private static final long serialVersionUID = 1L;

    public Object resolveAddress(String actorId) {
      if ("ghost".equals(actorId)) return null;
      return actorId + "@example.domain";
    }
  }
}
