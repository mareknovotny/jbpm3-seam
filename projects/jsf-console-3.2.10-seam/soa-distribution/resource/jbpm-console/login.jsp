<%
    if (request.getRemoteUser() != null) {
        response.sendRedirect("app/");
        return;
    }
    // System.out.println("PATH: " + request.getRequestURI());

%>
<html>
<head>
    <title>JBoss SOA Login</title>

    <style type="text/css">
        body {
            font-family: sans-serif;
            margin-left: 0;
            margin-right: 0;

            padding-left: 0;
            padding-right: 0;

        }

        img {
            border: 0;
        }

        #content {
            padding: 15px;
        }

        h3 {
            color: #8c8c8c;
        }

        a:link, a:visited, a {
            font-weight: bold;
            color: #333333;
        }

        a:hover {
            color: slategray;
        }

        #topsep {
            width: 100%;
            background: url( '/Gradient.jpg' );
        }

        #footer {
            text-align: center;
            font-size: 10px;
        }

    </style>

</head>
<body>
<a href="/"><img src="/jbpm-console/JBossLogo.jpg" alt="JBoss SOA Platform"/></a>

<div id="topsep">&nbsp;</div>

<div style="border: 1px solid darkgray; background: #ff8c00; font-weight: bold; padding: 10px; margin: 5px">
    You must provide security credentials to access this management console.
</div>

<% if ("1".equals(request.getParameter("error"))) { %>
<div style="border: 1px solid darkgray; color: white; background: darkred; font-weight: bold; padding: 10px; margin: 5px">
    Invalid Login/Password. Please Try Again.
</div>
<% }%>

<div align="center" style="border: 1px solid darkgray; background-color: gainsboro; font-size: 11px; padding: 15px;">
    <form name="loginform" method="post" action="j_security_check">
        <table class="leftmenu">
            <tbody>
                <tr class="leftmenu">
                    <th>User Name</th>
                    <td>
                        <input name="j_username" type="text"/>
                    </td>
                </tr>
                <tr class="leftmenu">
                    <th>Password</th>
                    <td>
                        <input name="j_password" type="password"/>
                    </td>
                </tr>
                <tr class="leftmenu">
                    <th/>
                    <td>
                        <input type="submit" value="Log In"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </form>
</div>
</body>
</html>