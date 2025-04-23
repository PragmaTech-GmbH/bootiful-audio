package digital.pragmatech;


import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | Speech App")
@AnonymousAllowed // Allow anyone to access the login page
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

  private final LoginForm login = new LoginForm();

  public LoginView() {
    addClassName("login-view");
    setSizeFull();
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);

    login.setAction("login"); // Point to Spring Security's default login endpoint

    add(new H1("Speech-to-Text Demo Login"), login);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    // Inform the user about login errors (e.g., bad credentials)
    if (beforeEnterEvent.getLocation()
      .getQueryParameters()
      .getParameters()
      .containsKey("error")) {
      login.setError(true);
    }
  }
}
