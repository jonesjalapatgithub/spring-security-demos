# spring-security-demos
<!-- wp:heading -->
<h2>Username &amp; Password Login with MySQL DB Storage<meta charset="utf-8"></h2>
<!-- /wp:heading -->

<!-- wp:paragraph -->
<p>Spring Security provides support for username and password being provided through an html form. </p>
<!-- /wp:paragraph -->

<!-- wp:heading {"level":3} -->
<h3>Step1: Add Dependencies  &amp; properties</h3>
<!-- /wp:heading -->

<!-- wp:syntaxhighlighter/code {"language":"xml"} -->
<pre class="wp-block-syntaxhighlighter-code">         &lt;dependency>
			 &lt;groupId>org.springframework.boot&lt;/groupId>
			 &lt;artifactId>spring-boot-starter-data-jpa&lt;/artifactId>
        &lt;/dependency>
		&lt;dependency>
			&lt;groupId>org.springframework.boot&lt;/groupId>
			&lt;artifactId>spring-boot-starter-security&lt;/artifactId>
		&lt;/dependency>
		&lt;dependency>
			&lt;groupId>org.springframework.boot&lt;/groupId>
			&lt;artifactId>spring-boot-starter-web&lt;/artifactId>
		&lt;/dependency>
		&lt;dependency>
			&lt;groupId>mysql&lt;/groupId>
			&lt;artifactId>mysql-connector-java&lt;/artifactId>
			&lt;scope>runtime&lt;/scope>
		&lt;/dependency>
		&lt;dependency>
			&lt;groupId>org.projectlombok&lt;/groupId>
			&lt;artifactId>lombok&lt;/artifactId>
			&lt;optional>true&lt;/optional>
		&lt;/dependency></pre>
<!-- /wp:syntaxhighlighter/code -->

<!-- wp:paragraph -->
<p><strong>Decalring the properties</strong> will help Spring to Autoconfigure Datasource Bean.  </p>
<!-- /wp:paragraph -->

<!-- wp:syntaxhighlighter/code -->
<pre class="wp-block-syntaxhighlighter-code">spring.datasource.url=jdbc:mysql://localhost:3306/springsecurity
spring.datasource.username=shoppinguser
spring.datasource.password=shoppingP@ssw0rd
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.naming-strategy=org.hibernate.cfg.ImprovedNamingStrategy
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5Dialect</pre>
<!-- /wp:syntaxhighlighter/code -->

<!-- wp:heading {"level":3} -->
<h3><meta charset="utf-8">Step2: Create User Entity &amp; Repository </h3>
<!-- /wp:heading -->

<!-- wp:paragraph -->
<p><strong>Create User Entity</strong> to read Users from DB, Read more on different schemas that Spring supports from <a href="https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html">docs.</a> </p>
<!-- /wp:paragraph -->

<!-- wp:syntaxhighlighter/code {"language":"java"} -->
<pre class="wp-block-syntaxhighlighter-code">@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String userName;
    private String password;
    private boolean active;
    private String roles;
   ....... 
   }</pre>
<!-- /wp:syntaxhighlighter/code -->

<!-- wp:paragraph -->
<p><strong>Create JPA  Repository</strong></p>
<!-- /wp:paragraph -->

<!-- wp:syntaxhighlighter/code {"language":"java"} -->
<pre class="wp-block-syntaxhighlighter-code">public interface UserRepository extends JpaRepository&lt;User, Integer> {
	
    Optional&lt;User> findByUserName(String userName);

}</pre>
<!-- /wp:syntaxhighlighter/code -->

<!-- wp:paragraph -->
<p><strong>In the MySQL DB</strong>, table user,  insert two records of user</p>
<!-- /wp:paragraph -->

<!-- wp:syntaxhighlighter/code {"language":"sql"} -->
<pre class="wp-block-syntaxhighlighter-code">insert into user(id, active, password, roles, user_name) values (1,true,"anu", "ROLE_USER", "anu");
insert into user(id, active, password, roles, user_name) values (1,true,"jones", "ROLE_ADMIN", "jones");</pre>
<!-- /wp:syntaxhighlighter/code -->

<!-- wp:heading {"level":3} -->
<h3>Step3: Implement UserDetailsService &amp; UserDetails</h3>
<!-- /wp:heading -->

<!-- wp:paragraph -->
<p>The authentication&nbsp;<code>Filter</code> calls <code>AuthenticationManager</code>, and this finally&nbsp;calls loadUserByUsername in our Custom <meta charset="utf-8">UserDetailsService for retrieving a username, password, and other attributes for authenticating with a username and password.  We will be using a&nbsp;<a href="https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html#servlet-authentication-jdbc">JDBC</a>&nbsp;implementations of&nbsp;<code>UserDetailsService</code> here in our example.</p>
<!-- /wp:paragraph -->

<!-- wp:paragraph -->
<p><strong>MyUserDetailsService</strong></p>
<!-- /wp:paragraph -->

<!-- wp:syntaxhighlighter/code {"language":"java"} -->
<pre class="wp-block-syntaxhighlighter-code">@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Optional&lt;User> user = userRepository.findByUserName(userName);

        user.orElseThrow(() -> new UsernameNotFoundException("Not found: " + userName));

        return user.map(MyUserDetails::new).get();
    }
}</pre>
<!-- /wp:syntaxhighlighter/code -->

<!-- wp:paragraph -->
<p> <strong>Custom MyUserDetails</strong> to Map User</p>
<!-- /wp:paragraph -->

<!-- wp:syntaxhighlighter/code {"language":"java"} -->
<pre class="wp-block-syntaxhighlighter-code">public class MyUserDetails implements UserDetails {

    private String userName;
    private String password;
    private boolean active;
    private List&lt;GrantedAuthority> authorities;

    
    public MyUserDetails(User user) {
        this.userName = user.getUserName();
        this.password = user.getPassword();
        this.active = user.isActive();
        this.authorities = Arrays.stream(user.getRoles().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
    }

.....</pre>
<!-- /wp:syntaxhighlighter/code -->

<!-- wp:heading {"level":3} -->
<h3><meta charset="utf-8">Step4: Configure SecurityConfigurion</h3>
<!-- /wp:heading -->

<!-- wp:paragraph -->
<p>EnableWebSecurity : This creates Spring security configuration and this creates a Servlet Filter known as the&nbsp;<code>springSecurityFilterChain</code>&nbsp;which is responsible for all the security (protecting the application URLs, validating submitted username and passwords, redirecting to the log in form, etc) within your application. </p>
<!-- /wp:paragraph -->

<!-- wp:paragraph -->
<p>The configuration in  configure(AuthenticationManagerBuilder auth) attempts to obtain the AuthenticationManager for authentication Purposes in MyUserDetailsService,  whereas Authorization is being handled at configure(HttpSecurity http)</p>
<!-- /wp:paragraph -->

<!-- wp:syntaxhighlighter/code {"language":"java"} -->
<pre class="wp-block-syntaxhighlighter-code">@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin").hasRole("ADMIN")
                .antMatchers("/user").hasAnyRole("ADMIN", "USER")
                .antMatchers("/").permitAll()
                .and().formLogin();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}</pre>
<!-- /wp:syntaxhighlighter/code -->

<!-- wp:heading {"level":3} -->
<h3><meta charset="utf-8">Step5: Run the Application</h3>
<!-- /wp:heading -->

<!-- wp:paragraph -->
<p>Add controller for testing</p>
<!-- /wp:paragraph -->

<!-- wp:syntaxhighlighter/code {"language":"java"} -->
<pre class="wp-block-syntaxhighlighter-code">@RestController
public class HomeController {
	
	@GetMapping("/")
	public String hello() {
		return ("&lt;h1>Welcome&lt;/h1>");
	}
	
	@GetMapping("/user")
	public String user() {
		return ("&lt;h1>Welcome User&lt;/h1>");
	}
	
	@GetMapping("/admin")
	public String admin() {
		return ("&lt;h1>Welcome admin&lt;/h1>");
	}
}
</pre>
<!-- /wp:syntaxhighlighter/code -->

<!-- wp:paragraph -->
<p>Hit the APP http://localhost:8080/user </p>
<!-- /wp:paragraph -->

<!-- wp:image {"id":1688,"sizeSlug":"full","linkDestination":"none"} -->
<figure class="wp-block-image size-full"><img src="https://www.jonesjalapat.com/wp-content/uploads/2021/11/Screenshot-2021-11-30-at-8.51.23-PM.png" alt="" class="wp-image-1688"/></figure>
<!-- /wp:image -->
