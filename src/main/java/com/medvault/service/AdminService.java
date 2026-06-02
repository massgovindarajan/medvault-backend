//package com.medvault.service;
//
//import com.medvault.dto.request.ApprovalRequest;
//import com.medvault.dto.response.ApiResponse;
//import com.medvault.dto.response.UserSummaryResponse;
//
//import java.util.List;
//
//public interface AdminService {
//
//    // L1 Admin – see users pending personal detail review
//    List<UserSummaryResponse> getL1PendingUsers();
//
//    // L2 Admin – see users where L1 approved, pending document review
//    List<UserSummaryResponse> getL2PendingUsers();
//
//    // L1 Admin action
//    ApiResponse<String> l1Approve(Long adminId, ApprovalRequest request);
//
//    // L2 Admin action – if both approved, activate and generate credentials
//    ApiResponse<String> l2Approve(Long adminId, ApprovalRequest request);
//
//    // Get all users (for ADMIN dashboard)
//    List<UserSummaryResponse> getAllUsers();
//
//    // Get single user detail
//    UserSummaryResponse getUserDetail(Long userId);
//}

package com.medvault.service;

import com.medvault.dto.request.ApprovalRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.UserSummaryResponse;
import java.util.List;

public interface AdminService {
    List<UserSummaryResponse> getL1PendingUsers();
    List<UserSummaryResponse> getL2PendingUsers();
    ApiResponse<String> l1Approve(Long adminId, ApprovalRequest request);
    ApiResponse<String> l2Approve(Long adminId, ApprovalRequest request);
    List<UserSummaryResponse> getAllUsers();
    UserSummaryResponse getUserDetail(Long userId);
    ApiResponse<String> deleteUser(Long userId); // ✅ NEW
}