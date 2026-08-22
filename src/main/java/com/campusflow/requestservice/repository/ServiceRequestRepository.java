package com.campusflow.requestservice.repository;

import com.campusflow.requestservice.document.RequestStatus;
import com.campusflow.requestservice.document.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRequestRepository extends MongoRepository<ServiceRequest, String> {

    Page<ServiceRequest> findByStudentId(String studentId, Pageable pageable);

    Page<ServiceRequest> findByStatus(RequestStatus status, Pageable pageable);

    Page<ServiceRequest> findByAssignedStaffId(String staffId, Pageable pageable);

    Page<ServiceRequest> findByStatusAndAssignedStaffId(RequestStatus status, String staffId, Pageable pageable);

    long countByStatus(RequestStatus status);
}
