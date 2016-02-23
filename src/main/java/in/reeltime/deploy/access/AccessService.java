package in.reeltime.deploy.access;

import in.reeltime.deploy.access.role.RoleService;

public class AccessService {

    private final RoleService roleService;

    public AccessService(RoleService roleService) {
        this.roleService = roleService;
    }

    public void setupAccess() {
    }
}
