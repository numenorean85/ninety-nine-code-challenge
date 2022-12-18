package com.ninetynine.codechallenge.company.api

import com.ninetynine.codechallenge.company.dto.CompanyDTO
import com.ninetynine.codechallenge.company.dto.CompanyDetailsCriteria
import com.ninetynine.codechallenge.company.dto.CompanyLightDTO
import com.ninetynine.codechallenge.company.service.CompanyService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/companies")
class CompanyController(
    private val companyService: CompanyService
) {

    @GetMapping("")
    fun getAllCompanies(pageable: Pageable): Page<CompanyLightDTO> {
        return companyService.getAllCompanies(pageable)
    }

    @GetMapping("/{code}")
    fun getCompanyDetailsByCode(
        @PathVariable code: String,
        criteria: CompanyDetailsCriteria
    ): CompanyDTO {
        return companyService.getCompanyDetailsByCode(code, criteria)
    }

}
