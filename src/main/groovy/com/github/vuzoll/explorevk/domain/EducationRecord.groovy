package com.github.vuzoll.explorevk.domain

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = [ 'university', 'faculty' ])
class EducationRecord {

    University university
    Faculty faculty
    Integer graduationYear
}
