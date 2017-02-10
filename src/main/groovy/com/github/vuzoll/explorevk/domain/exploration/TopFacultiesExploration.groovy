package com.github.vuzoll.explorevk.domain.exploration

import com.github.vuzoll.explorevk.domain.vk.VkFaculty

class TopFacultiesExploration extends VkDatasetExploration {

    Integer numberOfFacultiesToTake

    Distribution<VkFaculty> facultyDistribution
}
