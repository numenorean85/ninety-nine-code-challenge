package com.ninetynine.codechallenge.company.persistance

import com.ninetynine.codechallenge.company.model.CompanyEntity
import com.ninetynine.codechallenge.company.model.CompanySharePriceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface CompanyRepository : JpaRepository<CompanyEntity, Long> {
    fun findByCode(code: String): CompanyEntity?
}

@Repository
interface CompanySharePriceRepository : JpaRepository<CompanySharePriceEntity, Long> {

    @Query("""select sp from CompanySharePriceEntity sp
        where sp.company.id = ?1 and sp.createdAt >= ?2 and sp.createdAt <= ?3
        order by sp.createdAt asc
    """)
    fun getSharePricesByCompanyAndPeriod(
        companyId: Long,
        startInstant: Instant,
        endInstant: Instant,
    ): List<CompanySharePriceEntity>
}