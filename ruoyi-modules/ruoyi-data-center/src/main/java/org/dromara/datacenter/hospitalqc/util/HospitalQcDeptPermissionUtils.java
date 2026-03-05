package org.dromara.datacenter.hospitalqc.util;

import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hospital QC department permission utilities.
 * Provides department-based data isolation and permission checking.
 */
@Slf4j
public final class HospitalQcDeptPermissionUtils {

    private HospitalQcDeptPermissionUtils() {
    }

    /**
     * Check if current user is administrator (super admin or tenant admin).
     *
     * @return true if admin
     */
    public static boolean isAdmin() {
        return LoginHelper.isSuperAdmin() || LoginHelper.isTenantAdmin();
    }

    /**
     * Get current user's department ID.
     *
     * @return department ID
     */
    public static Long getCurrentDeptId() {
        return LoginHelper.getDeptId();
    }

    /**
     * Get current user's department name.
     *
     * @return department name
     */
    public static String getCurrentDeptName() {
        return LoginHelper.getDeptName();
    }

    /**
     * Get allowed department IDs for current user.
     * For admin: returns empty set (meaning all departments allowed)
     * For normal user: returns current department ID
     *
     * @return set of allowed department IDs, empty means all
     */
    public static Set<Long> getAllowedDeptIds() {
        if (isAdmin()) {
            return Collections.emptySet();
        }
        Long currentDeptId = getCurrentDeptId();
        if (currentDeptId == null) {
            throw new ServiceException("当前用户未分配科室");
        }
        return new HashSet<>(List.of(currentDeptId));
    }

    /**
     * Validate if the requested department IDs are within allowed scope.
     *
     * @param requestedDeptIds requested department IDs
     * @return filtered department IDs
     * @throws ServiceException if no permission
     */
    public static Set<Long> validateAndFilterDeptIds(List<Long> requestedDeptIds) {
        Set<Long> allowedDeptIds = getAllowedDeptIds();

        // Admin can access all, no filtering needed
        if (allowedDeptIds.isEmpty()) {
            if (requestedDeptIds == null || requestedDeptIds.isEmpty()) {
                return Collections.emptySet();
            }
            return new HashSet<>(requestedDeptIds);
        }

        // Normal user: must filter by allowed departments
        if (requestedDeptIds == null || requestedDeptIds.isEmpty()) {
            // Default to allowed departments
            return allowedDeptIds;
        }

        // Validate each requested department
        Set<Long> filtered = new HashSet<>();
        for (Long deptId : requestedDeptIds) {
            if (deptId == null) {
                continue;
            }
            if (allowedDeptIds.contains(deptId)) {
                filtered.add(deptId);
            } else {
                log.warn("User {} attempted to access unauthorized department: {}",
                    LoginHelper.getUserId(), deptId);
            }
        }

        if (filtered.isEmpty()) {
            throw new ServiceException("没有权限访问请求的科室数据");
        }

        return filtered;
    }

    /**
     * Check if user has permission to access specific department.
     *
     * @param deptId department ID to check
     * @return true if has permission
     */
    public static boolean hasDeptPermission(Long deptId) {
        if (deptId == null) {
            return true;
        }
        Set<Long> allowedDeptIds = getAllowedDeptIds();
        if (allowedDeptIds.isEmpty()) {
            return true; // Admin can access all
        }
        return allowedDeptIds.contains(deptId);
    }

    /**
     * Assert user has permission to access specific department.
     *
     * @param deptId department ID to check
     * @throws ServiceException if no permission
     */
    public static void assertDeptPermission(Long deptId) {
        if (!hasDeptPermission(deptId)) {
            throw new ServiceException("没有权限访问该科室数据");
        }
    }

    /**
     * Build department context info for frontend.
     */
    public record DeptContextInfo(
        boolean isAdmin,
        Long currentDeptId,
        String currentDeptName,
        List<Long> allowedDeptIds
    ) {
    }

    /**
     * Get department context info for current user.
     *
     * @return DeptContextInfo
     */
    public static DeptContextInfo getDeptContextInfo() {
        boolean isAdmin = isAdmin();
        Long currentDeptId = getCurrentDeptId();
        String currentDeptName = getCurrentDeptName();
        Set<Long> allowed = getAllowedDeptIds();
        List<Long> allowedList = allowed.isEmpty() ? Collections.emptyList() : List.copyOf(allowed);

        return new DeptContextInfo(isAdmin, currentDeptId, currentDeptName, allowedList);
    }
}
