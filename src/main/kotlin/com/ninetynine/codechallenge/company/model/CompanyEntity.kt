package com.ninetynine.codechallenge.company.model

import jakarta.persistence.*

@Entity
@Table(name = "company")
class CompanyEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
) {
    @Column(name = "name")
    var name: String? = null

    @Column(name = "code")
    var code: String? = null

    @Column(name = "description")
    var description: String? = null

    @OneToOne(mappedBy = "company", fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var latestSharePrice: CompanyLatestSharePriceEntity? = null
}