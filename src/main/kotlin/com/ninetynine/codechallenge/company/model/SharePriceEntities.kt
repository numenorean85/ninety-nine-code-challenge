package com.ninetynine.codechallenge.company.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "company_share_price")
class CompanySharePriceEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
) {
    @Column(name = "share_price")
    var sharePrice: BigDecimal? = null

    // TODO Optimization: add a JpaConverter to convert strings in Java Currency objects
    @Column(name = "currency")
    var currency: String? = null

    @Column(name = "created_at")
    lateinit var createdAt: Instant

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    lateinit var company: CompanyEntity
}

@Entity
@Table(name = "company_latest_share_price")
class CompanyLatestSharePriceEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
) {
    @Column(name = "share_price")
    var sharePrice: BigDecimal? = null

    // TODO Optimization: add a JpaConverter to convert strings in Java Currency objects
    @Column(name = "currency")
    var currency: String? = null

    @Column(name = "updated_at")
    lateinit var lastUpdate: Instant

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    lateinit var company: CompanyEntity
}