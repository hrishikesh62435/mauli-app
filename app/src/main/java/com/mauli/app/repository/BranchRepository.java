package com.mauli.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mauli.app.model.Branch;

public interface BranchRepository extends JpaRepository<Branch, Integer> {

}
