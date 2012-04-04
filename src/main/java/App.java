import java.io.IOException;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;

public class App extends HttpServlet {

    @Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
				Random generator = new Random();
				int digit = generator.nextInt(30);

				StringBuilder sb = new StringBuilder();
        sb.append("<h1>MemCachier Fibonacci Example</h1>");
				sb.append("<p>This script computes a random digit of the Fibonacci sequence.  Before computing, though, it checks to see if there's a cached value for the digit and serves the cached value if there's a hit.</p>");
				sb.append("<p>Digit: " + digit + "</p>");
				sb.append("<p>Value: " + fibDigit(digit) + "</p>");

        resp.getWriter().print(sb.toString());
    }

    public static void main(String[] args) throws Exception{
        Server server = new Server(Integer.valueOf(System.getenv("PORT")));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new App()),"/*");
        server.start();
        server.join();   
    }

		private static int fibDigit(int n) {
				if (n < 2) {
						return n;
				} else {
						return fibDigit(n - 1) + fibDigit(n - 2);
				}
		}
}