package in.reeltime.deploy.access.role;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.*;
import com.google.common.collect.Maps;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import in.reeltime.deploy.resource.ResourceService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class RoleService {

    private static final String ROLE_FORMAT = "/roles/%s.json";
    private static final String ROLE_POLICY_FORMAT = "/role-policies/%s.json";

    private final AmazonIdentityManagement iam;
    private final ResourceService resourceService;

    public RoleService(AmazonIdentityManagement iam, ResourceService resourceService) {
        this.iam = iam;
        this.resourceService = resourceService;
    }

    public Role createRole(String roleName, String policyDocumentName) {
        String policyDocument = getPolicyDocument(ROLE_FORMAT, policyDocumentName);

        CreateRoleRequest request = new CreateRoleRequest()
                .withAssumeRolePolicyDocument(policyDocument)
                .withRoleName(roleName);

        CreateRoleResult result = iam.createRole(request);
        return result.getRole();
    }

    public Role addPolicy(Role role, String policyName, String policyDocumentName) {
        String roleName = role.getRoleName();
        String policyDocument = getPolicyDocument(ROLE_POLICY_FORMAT, policyDocumentName);

        PutRolePolicyRequest request = new PutRolePolicyRequest()
                .withRoleName(roleName)
                .withPolicyName(policyName)
                .withPolicyDocument(policyDocument);

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
