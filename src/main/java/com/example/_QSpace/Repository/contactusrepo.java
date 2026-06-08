package com.example._QSpace.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example._QSpace.Entity.Contactus;

@Repository
public interface contactusrepo extends JpaRepository<Contactus, String> {

}
