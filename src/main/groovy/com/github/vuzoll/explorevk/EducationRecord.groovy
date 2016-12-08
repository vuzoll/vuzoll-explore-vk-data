package com.github.vuzoll.explorevk

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = [ 'university', 'faculty' ])
class EducationRecord {

    University university
    Faculty faculty
    Integer graduationYear
}
