package io.archton.scaffold.service;

import io.archton.scaffold.repository.UserLoginRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ActiveUserAugmentor implements SecurityIdentityAugmentor {

    @Inject
    UserLoginRepository userLoginRepository;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }
        return context.runBlocking(() -> {
            String email = identity.getPrincipal().getName();
            boolean active = QuarkusTransaction.requiringNew().call(() ->
                userLoginRepository.findByEmail(email)
                    .map(u -> u.active)
                    .orElse(false));
            if (!active) {
                throw new AuthenticationFailedException("Account is inactive");
            }
            return identity;
        });
    }
}
