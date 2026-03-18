public ResponseEntity<?> login(String username, String password) {
    // validation
    if (username == null || username.isEmpty()) return ResponseEntity.badRequest().build();
    // requête base
    String query = "SELECT * FROM users WHERE username = '" + username + "'";
    User user = db.query(query);
    // vérification mot de passe
    if (!user.getPassword().equals(password)) return ResponseEntity.status(401).build();
    // création token
    String token = UUID.randomUUID().toString();
    sessions.put(token, user.getId());
    // log
    log.info("User logged in: " + username + " password=" + password);
    return ResponseEntity.ok(token);
}

public ResponseEntity<?> login(String username, String password) {
    validateInputs(username, password);
    User user = userRepository.findByUsername(username)
    .orElseThrow(() -> new AuthenticationException("User not found"));
    verifyPassword(password, user.getPassword());
    String token = sessionService.createSession(user.getId());
    auditLog.LogSuccessfulLogin(username);
    return ResponseEntity.ok(token);

}


if (role.equals("1")) {
    // admin access
}

String jwt = Jwts.builder()
    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
    .signWith(Keys.hmacShaKeyFor("secret".getBytes()))
    .compact();

public enum Role {
    USER,
    ADMIN,
    FORMATEUR
}

if(user.getRole() == Role.ADMIN) {
    // admin access
}

private static final Duration TOKEN_VALIDITY = Duration.ofHours(1);
private static final Key SIGNING_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

string jwt = Jwts.builder()
    .setExpiration(Date.from(Instant.now().plus(TOKEN_VALIDITY)))
    .signWith(SIGNING_KEY)
    .compact();


class A {

    public void methodA() {
        // code
    }
}

class B extends A {
    public void methodA() {
        // code
    }
}

class C extends B {

    public void methodA() {
        // code
    }
}

List<A> list = new ArrayList<>();
list.add(new A());
list.add(new B());
list.add(new C());
for (A a : list) {
    a.methodA();
}

public void createUser(String username, String password, Role role) {
    if (username == null || username.isEmpty()) throw new IllegalArgumentException("Username is required");
    if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password is required");
    if (role == null) throw new IllegalArgumentException("Role is required");
    String hashedPassword = passwordEncoder.encode(password);
    User user = new User(username, hashedPassword, role);
    userRepository.save(user);
}

public void editUser(UserDTO userDTO) {
    if(userDTO.isValid()) {
        User user = userRepository.findById(userDTO.getId())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
        userRepository.save(user);
    } else {
        throw new IllegalArgumentException("Invalid user data");
    }
}
public void editUser(String username, String newPassword, Role newRole) {
    if (username == null || username.isEmpty()) throw new IllegalArgumentException("Username is required");
    if (newPassword == null || newPassword.isEmpty()) throw new IllegalArgumentException("New password is required");
    if (newRole == null) throw new IllegalArgumentException("New role is required");
    User user = userRepository.findByUsername(username)
    .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (newPassword != null && !newPassword.isEmpty()) {
        user.setPassword(passwordEncoder.encode(newPassword));
    }
    if (newRole != null) {
        user.setRole(newRole);
    }
    userRepository.save(user);
}

// Vérification de rôle (temporairement désactivée)
// if(!user.haseRole(Role.ADMIN)) throw new AccessDeniedException("Admin role required");

abstract class User {
    public abstract boolean hasRole(Role role);
    public abstract String getUsername();
}

class AdminUser extends User {
    @Override
    public boolean hasRole(Role role) {
        return role == Role.ADMIN;
    }
    @Override
    public String getUsername() {
        return "admin";
    }
}

class RegularUser extends User {
    @Override
    public boolean hasRole(Role role) {
        throw new UnsupportedOperationException("Regular users do not have roles");
    }
    @Override
    public String getUsername() {
        return "user";
    }
}