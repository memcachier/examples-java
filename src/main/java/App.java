import java.io.IOException;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.auth.PlainCallbackHandler;
import net.spy.memcached.auth.AuthDescriptor;

public class App extends HttpServlet {

    @Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
				Random generator = new Random();
				int digit = generator.nextInt(30);
				Result r = findOrComputeDigit(digit);

				StringBuilder sb = new StringBuilder();
        sb.append("<h1>MemCachier Fibonacci Example</h1>");
				sb.append("<p>This script computes a random digit of the Fibonacci sequence.  Before computing, though, it checks to see if there's a cached value for the digit and serves the cached value if there's a hit.</p>");
				sb.append("<p>Digit: " + digit + "</p>");
				sb.append("<p>Value: " + r.val + "</p>");
				sb.append("<p>Was in cache? " + r.cached + "</p>");

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

		private static Result findOrComputeDigit(int n) {
				AuthDescriptor ad = new AuthDescriptor(new String[]{"PLAIN"},
																							 new PlainCallbackHandler(System.getenv("MEMCACHIER_USERNAME"),
																																				System.getenv("MEMCACHIER_PASSWORD")));

        try {
						MemcachedClient mc = new MemcachedClient(
																		 new ConnectionFactoryBuilder().setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
																		 .setAuthDescriptor(ad)
																		 .build(),
																		 AddrUtil.getAddresses(System.getenv("MEMCACHIER_SERVERS") + ":11211"));
						Object inCache = mc.get("" + n);
						if (inCache == null) {
								return new Result(computeAndSet(mc, n), false);
						} else {
								return new Result((Integer) inCache, true);
						}
        } catch (IOException ex) {
            System.err.println("Couldn't create a connection, bailing out: \nIOException " + ex.getMessage());
						return new Result(fibDigit(n), false);
        }
		}

		private static int computeAndSet(MemcachedClient mc, int n) {
				int val = fibDigit(n);
				mc.set("" + n, 3600, val);
				return val;
		}

		private static int fibDigit(int n) {
				if (n < 2) {
						return n;
				} else {
						return fibDigit(n - 1) + fibDigit(n - 2);
				}
		}

		private static class Result {
				public int val;
				public boolean cached;

				public Result(int val, boolean cached) {
						this.val = val;
						this.cached = cached;
				}
		}
}