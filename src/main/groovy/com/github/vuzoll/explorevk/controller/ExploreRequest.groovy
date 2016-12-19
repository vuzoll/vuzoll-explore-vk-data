package com.github.vuzoll.explorevk.controller

import groovy.transform.ToString

@ToString(includeNames = true, ignoreNulls = true)
class ExploreRequest {

    Integer topNUniversitiesLimit
    Integer topNFacultiesLimit
}
