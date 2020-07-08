package org.patientview.api.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.patientview.api.annotation.AuditTrail;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.BaseModel;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * TODO for now when can add an extra option on the main annotation to describe the class is auditing.
 * Ideally we could add an extra annotation on the parameter to audit to make this more flexible.
 *
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
@Aspect
@Configurable
public class AuditAspect {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAspect.class);

    private static AuditAspect instance;

    @Inject
    private AuditService auditService;

    @Inject
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        // when we cannot get an authentication object default to system object
        LOG.info("Audit aspect started");
    }

    @Pointcut("execution(public * *(..))")
    public void publicMethod() {
    }

    // Singleton pattern for AspectJ vs Spring management
    public static AuditAspect aspectOf() {

        if (instance == null) {
            instance = new AuditAspect();
        }
        return instance;
    }

    @AfterReturning(value = "@annotation(org.patientview.api.annotation.AuditTrail)")
    public void auditObject(JoinPoint joinPoint) {

        AuditTrail auditTrail = getAuditAction(joinPoint);
        LOG.debug("Audit action...{}", auditTrail.value());

        BaseModel auditObject = getObject(joinPoint);

        // We get the id off the object or the Long value on the method
        Long id;
        if (auditObject == null) {
            id = getId(joinPoint);
        } else {
            id = auditObject.getId();
        }

        Audit audit = createAudit(auditTrail, id);
        auditService.save(audit);
    }

    private Audit createAudit(AuditTrail auditTrail, Long objectId) {

        User user = null;
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            }
        } catch (NullPointerException npe) {
            LOG.debug("Audit cannot get security context");
        }

        Audit audit = new Audit();
        audit.setSourceObjectId(objectId);

        // handle audit when no currently logged in user (e.g. verify email)
        if (user != null) {
            if (userRepository.existsById(user.getId())) {
                audit.setActorId(user.getId());
            }
        }

        for (AuditObjectTypes auditObjectType : AuditObjectTypes.class.getEnumConstants()) {
            if (auditObjectType.getName().equals(auditTrail.objectType().getSimpleName())) {
                audit.setSourceObjectType(auditObjectType);

                // set username if User type object
                if (auditObjectType.equals(AuditObjectTypes.User)) {
                    Optional<User> sourceObjectUser = userRepository.findById(objectId);
                    if (sourceObjectUser.isPresent()) {
                        audit.setUsername(sourceObjectUser.get().getUsername());
                    }
                }
            }
        }

        audit.setAuditActions(auditTrail.value());
        return audit;
    }

    // Retrieve the list of Roles from the annotation.
    public static AuditTrail getAuditAction(JoinPoint joinPoint) {
        final org.aspectj.lang.Signature signature = joinPoint.getStaticPart().getSignature();
        if (signature instanceof MethodSignature) {
            final MethodSignature ms = (MethodSignature) signature;

            for (Annotation annotation : ms.getMethod().getDeclaredAnnotations()) {
                if (annotation.annotationType() == AuditTrail.class) {
                    return ((AuditTrail) annotation);
                }
            }
        }
        return null;
    }


    // Assuming we apply the annotation to a method with a Object
    private BaseModel getObject(JoinPoint joinPoint) {

        for (Object argument : joinPoint.getArgs()) {
            if (argument != null && BaseModel.class.isAssignableFrom(argument.getClass())) {
                return (BaseModel) argument;
            }
        }

        return null;
    }

    // Assuming we apply the annotation to a method with a Id, class type is in the annotation
    private Long getId(JoinPoint joinPoint) {

        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof Long) {
                return (Long) argument;
            }
        }

        return null;
    }

    // TODO Sprint 3
    // Assuming we apply the annotation to a method with a String, class type is in the annotation
    private String getString(JoinPoint joinPoint) {

        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof String) {
                return (String) argument;
            }
        }

        return null;
    }

}
