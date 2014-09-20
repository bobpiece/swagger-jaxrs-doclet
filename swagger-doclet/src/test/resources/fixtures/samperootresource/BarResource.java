package fixtures.samperootresource;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/foo")
@SuppressWarnings("javadoc")
public class BarResource {

  @POST
  @Path("annotated")
  public String sayHello(@QueryParam("name") String name) {
    return "Hello, " + name + "!";
  }
}
