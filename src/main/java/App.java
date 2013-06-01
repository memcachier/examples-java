import java.io.IOException;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;

/**
 * MemCachier (minimal) Java example application.
 *
 * @author MemcCachier Inc.
 */
public class App extends HttpServlet {

  /**
   * Respond to a (any) HTTP GET request.
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    int digit = (new Random()).nextInt(50);
    Result r = fib(digit);

    StringBuilder sb = new StringBuilder();
    sb.append("<html><head><title>MemCachier Fibonacci Example</title></head><body>");
    sb.append("<h1>MemCachier Fibonacci Example</h1>");
    sb.append("<p>This script computes a random digit of the Fibonacci " +
       "sequence. Before computing, though, it checks to see if there's " +
       "a cached value for the digit and serves the cached value if " +
       "there's a hit.</p>");
    sb.append("<p>Digit: " + digit + "</p>");
    sb.append("<p>Value: " + r.val + "</p>");
    sb.append("<p>Was in cache? " + r.cached + "</p></body></html>");

    resp.getWriter().print(sb.toString());
  }

  /**
  * Start the server.
  */
  public static void main(String[] args) throws Exception {
    Server server = new Server(Integer.valueOf(System.getenv("PORT")));
    ServletContextHandler context = new ServletContextHandler(
        ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);
    context.addServlet(new ServletHolder(new App()), "/*");

    // ==============================
    // Connect to MemCachier
    // ==============================
    createMemCachierClient();
    // ==============================

    server.start();
    server.join();
  }

  /**
  * A class for storing fibonacci results, indicating if they are cached or not.
  */
  private static class Result {
    public int val;
    public boolean cached;

    public Result(int val, boolean cached) {
      this.val = val;
      this.cached = cached;
    }
  }

  // ==============================
  // Memcachier connection
  // ==============================
  private static MemcachedClient mc;
  // ==============================

  /**
   * Create a memcachier connection
   */
  private static void createMemCachierClient() {
    try {
      AuthDescriptor ad = new AuthDescriptor(
          new String[] { "PLAIN" },
          new PlainCallbackHandler(System.getenv("MEMCACHIER_USERNAME"),
                                   System.getenv("MEMCACHIER_PASSWORD")));
      mc = new MemcachedClient(
          new ConnectionFactoryBuilder().setProtocol(
              ConnectionFactoryBuilder.Protocol.BINARY).setAuthDescriptor(ad).build(),
          AddrUtil.getAddresses(System.getenv("MEMCACHIER_SERVERS")));
    } catch (Exception ex) {
      System.err.println(
        "Couldn't create a connection, bailing out:\nIOException "
        + ex.getMessage());
    }
  }

  /**
  * Computes a fibonacci result of {@code n}, checking the cache for any answers.
  *
  * Note: You'd actually want to check the cache on recursive calls, but we
  * don't do that here for simplicity.
  */
  private static Result fib(int n) {
    try {
      Object inCache = mc.get("" + n);
      if (inCache == null) {
        return new Result(computeAndSet(n), false);
      } else {
        return new Result((Integer) inCache, true);
      }
    } catch (Exception e) {
      // if any exception we simply shutdown the existing one an reconnect.
      // XXX: This is a very hacky way of dealing with the problem, thought
      // needs to be put in to your application to handle failures gracefully.
      mc.shutdown();
      createMemCachierClient();
    }

    return new Result(fibRaw(n), false);
  }

  /**
   * Perform fibonacci compuatation and store result in cache.
   */
  private static int computeAndSet(int n) {
    int val = fibRaw(n);
    mc.set("" + n, 3600, val);
    return val;
  }

  /**
   * Perform actual fibonacci compuatation.
   */
  private static int fibRaw(int n) {
    if (n < 2) {
      return n;
    } else {
      return fibRaw(n - 1) + fibRaw(n - 2);
    }
  }

}

/* vim: set sw=2 ts=2 sts=2 expandtab: */

