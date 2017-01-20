package com.github.vuzoll.explorevk.domain.vk

import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

@EqualsAndHashCode(includes = 'facultyId')
@Builder
class VkFaculty {

    VkUniversity university
    Integer facultyId
    String facultyName
}
