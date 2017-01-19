package com.github.vuzoll.explorevk.domain.vk

import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

@EqualsAndHashCode
@Builder
class VkUniversity {

    Integer universityId
    Integer countryId
    Integer cityId
    String universityName
}
