package com.shoppingcart.gateway.config;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;
import java.util.Set;

/**
 * Who may call what. This is the plan's endpoint table, in code.
 *
 * The list is ordered and the first match wins, so a narrow rule has to sit above the broad rule
 * it carves out of. GET /products/my is above GET /products/**, or a shopowner's own catalog
 * would resolve as a public browse.
 */
@Component
public class RouteRules {

    /** Anyone at all, with or without a token. */
    private static final Set<String> ANY = Set.of("*");

    /** Any signed-in user, whatever the role. */
    private static final Set<String> AUTHENTICATED = Set.of("USER", "SHOPOWNER", "ADMIN");

    private final List<Rule> rules;

    public RouteRules() {
        PathPatternParser parser = new PathPatternParser();
        this.rules = List.of(
                // public
                rule(parser, HttpMethod.POST, "/auth/register", ANY),
                rule(parser, HttpMethod.POST, "/auth/login", ANY),

                // auth, admin only
                rule(parser, HttpMethod.GET, "/auth/users", Set.of("ADMIN")),
                rule(parser, HttpMethod.PUT, "/auth/users/*/deactivate", Set.of("ADMIN")),

                // profile
                rule(parser, null, "/profiles/me/**", AUTHENTICATED),
                rule(parser, HttpMethod.GET, "/profiles", Set.of("ADMIN")),
                rule(parser, HttpMethod.GET, "/profiles/*", Set.of("ADMIN")),

                // product: the narrow rules first
                rule(parser, HttpMethod.GET, "/products/my", Set.of("SHOPOWNER")),
                rule(parser, HttpMethod.POST, "/products", Set.of("SHOPOWNER")),
                rule(parser, HttpMethod.PUT, "/products/*", Set.of("SHOPOWNER")),
                rule(parser, HttpMethod.DELETE, "/products/*", Set.of("SHOPOWNER", "ADMIN")),
                rule(parser, HttpMethod.GET, "/products/**", ANY),

                // cart
                rule(parser, null, "/cart/**", Set.of("USER")),
                rule(parser, null, "/cart", Set.of("USER")),

                // order: narrow rules first again
                rule(parser, HttpMethod.GET, "/orders/received", Set.of("SHOPOWNER")),
                rule(parser, HttpMethod.PUT, "/orders/*/status", Set.of("SHOPOWNER")),
                rule(parser, HttpMethod.GET, "/orders/my", Set.of("USER")),
                rule(parser, HttpMethod.POST, "/orders/address", Set.of("USER")),
                rule(parser, HttpMethod.GET, "/orders/address", Set.of("USER")),
                rule(parser, HttpMethod.PUT, "/orders/*/cancel", Set.of("USER")),
                rule(parser, HttpMethod.POST, "/orders/checkout", Set.of("USER")),
                rule(parser, HttpMethod.GET, "/orders", Set.of("ADMIN"))
        );
    }

    private Rule rule(PathPatternParser parser, HttpMethod method, String path, Set<String> roles) {
        return new Rule(method, parser.parse(path), roles);
    }

    /** True when this path needs no token at all. */
    public boolean isPublic(HttpMethod method, org.springframework.http.server.PathContainer path) {
        Rule match = match(method, path);
        return match != null && match.roles == ANY;
    }

    /** True when the role may call this path. An unlisted path is denied. */
    public boolean isAllowed(HttpMethod method, org.springframework.http.server.PathContainer path, String role) {
        Rule match = match(method, path);
        return match != null && (match.roles == ANY || match.roles.contains(role));
    }

    private Rule match(HttpMethod method, org.springframework.http.server.PathContainer path) {
        for (Rule rule : rules) {
            if ((rule.method == null || rule.method.equals(method)) && rule.pattern.matches(path)) {
                return rule;
            }
        }
        return null;
    }

    private record Rule(HttpMethod method, PathPattern pattern, Set<String> roles) {
    }
}
