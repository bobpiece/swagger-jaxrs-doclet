package fixtures.samperootresource;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/foo")
@SuppressWarnings("javadoc")
public class FooResource {

  /**
   * @errorResponse 404 not found
   * @errorResponse ABC invalid code won't be parsed
   */
  @GET
  public String sayHello(@QueryParam("name") @DefaultValue("World") String name) {
    return "Hello, " + name + "!";
  }
}
