package in.reeltime.deploy.access.role;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.*;
import com.google.common.collect.Maps;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import in.reeltime.deploy.log.Logger;
import in.reeltime.deploy.resource.ResourceService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class RoleService {

    private static final String ROLE_FORMAT = "/roles/%s.json";
    private static final String ROLE_POLICY_FORMAT = "/role-policies/%s.json.template";

    private final AmazonIdentityManagement iam;
    private final ResourceService resourceService;

    public RoleService(AmazonIdentityManagement iam, ResourceService resourceService) {
        this.iam = iam;
        this.resourceService = resourceService;
    }

    public boolean roleExists(String roleName) {
        Logger.info("Checking existence of role: %s", roleName);
        return getRole(roleName) != null;
    }

    public Role getRole(String roleName) {
        Logger.info("Getting role: %s", roleName);
        List<Role> roles = iam.listRoles().getRoles();

        return roles.stream()
                .filter(r -> r.getRoleName().equals(roleName))
                .findFirst().get();
    }

    public Role createRole(String roleName, String policyDocumentName) {
        String policyDocument = getPolicyDocument(ROLE_FORMAT, policyDocumentName);

        CreateRoleRequest request = new CreateRoleRequest()
                .withAssumeRolePolicyDocument(policyDocument)
                .withRoleName(roleName);

        Logger.info("Creating role: %s", roleName);

        CreateRoleResult result = iam.createRole(request);
        return result.getRole();
    }

    public boolean roleHasPolicy(Role role, String policyName) {
        String roleName = role.getRoleName();

        ListRolePoliciesRequest request = new ListRolePoliciesRequest()
                .withRoleName(roleName);

        ListRolePoliciesResult result = iam.listRolePolicies(request);

        Logger.info("Checking role %s for policy %s", roleName, policyName);

        List<String> policyNames = result.getPolicyNames();
        return policyNames.contains(policyName);
    }

    public Role addPolicy(Role role, String policyName, String policyDocumentName, RolePolicyParameters parameters) {
        String roleName = role.getRoleName();
        String policyDocument = getPolicyDocument(ROLE_POLICY_FORMAT, policyDocumentName, parameters.toMap());

        PutRolePolicyRequest request = new PutRolePolicyRequest()
                .withRoleName(roleName)
                .withPolicyName(policyName)
                .withPolicyDocument(policyDocument);

        Logger.info("Adding policy %s to role %s", policyName, roleName);

        iam.putRolePolicy(request);
        return refreshRole(role);
    }

    private String getPolicyDocument(String format, String policyName) {
        return getPolicyDocument(format, policyName, Maps.newHashMap());
    }

    private String getPolicyDocument(String format, String policyDocumentName, Map<String, String> templateBindings) {
        try {
            String resourceName = String.format(format, policyDocumentName);
            InputStream resource = resourceService.getResource(resourceName);

            try (InputStreamReader reader = new InputStreamReader(resource)) {
                SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();

                Template template = templateEngine.createTemplate(reader);
                return template.make(templateBindings).toString();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to get policy document: " + policyDocumentName, e);
        }
    }

    private Role refreshRole(Role role) {
        String roleId = role.getRoleId();
        ListRolesResult result = iam.listRoles();

        return result.getRoles().stream()
                .filter(r -> r.getRoleId().equals(roleId))
                .findFirst().get();
    }
}
