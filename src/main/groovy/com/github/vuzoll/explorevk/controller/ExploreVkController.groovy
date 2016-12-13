package com.github.vuzoll.explorevk.controller

import com.github.vuzoll.explorevk.domain.City
import com.github.vuzoll.explorevk.domain.Country
import com.github.vuzoll.explorevk.domain.EducationRecord
import com.github.vuzoll.explorevk.domain.Faculty
import com.github.vuzoll.explorevk.domain.University
import com.github.vuzoll.explorevk.domain.VkProfile
import com.github.vuzoll.explorevk.service.ReportFilesService
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import java.util.concurrent.TimeUnit

@RestController
@Slf4j
class ExploreVkController {

    static String DATA_FILE_PATH = System.getenv('EXPLORE_VK_DATA_FILE_PATH') ?: '/data/vk.data'
    static String EXPLORATION_DIRECTORY_PATH = System.getenv('EXPLORE_VK_EXPLORATION_DIRECTORY_PATH') ?: '/data'
    static String UNIV_VS_COUNTRY_FILE_NAME = System.getenv('EXPLORE_VK_UNIV_VS_COUNTRY_FILE_NAME') ?: 'univ-vs-country.csv'
    static String UNIVERSITIES_FILE_NAME = System.getenv('EXPLORE_VK_UNIVERSITIES_FILE_NAME') ?: 'universities.csv'
    static String FACULTIES_FILE_NAME = System.getenv('EXPLORE_VK_FACULTIES_FILE_NAME') ?: 'faculties.csv'
    static String COUNTRIES_FILE_NAME = System.getenv('EXPLORE_VK_COUNTRIES_FILE_NAME') ?: 'countries.csv'
    static String CITIES_FILE_NAME = System.getenv('EXPLORE_VK_CITIES_FILE_NAME') ?: 'cities.csv'

    static Country UKRAINE = new Country(vkId: 2)

    JsonSlurper jsonSlurper = new JsonSlurper()
    File dataFile = new File(DATA_FILE_PATH)

    @Autowired
    ReportFilesService reportFilesService

    @RequestMapping(path = '/explore', method = RequestMethod.POST)
    @ResponseBody ExploreResponse explore() {
        log.info 'Generating exploration data...'

        long startTime = System.currentTimeMillis()
        int dataSetSize = 0
        int nonEmptyEducationRecords = 0
        int univVsCountrySize = 0

        Set<University> universities = []
        Set<Faculty> faculties = []
        Set<Country> countries = []
        Set<City> cities = []

        File univVsCountryFile = reportFilesService.createEmptyFile("$EXPLORATION_DIRECTORY_PATH/$UNIV_VS_COUNTRY_FILE_NAME")
        log.info "Generating $univVsCountryFile.path..."
        univVsCountryFile.text = 'person_city_id,person_country_id,faculty_id,university_id,university_city_id,university_country_id\n'
        dataFile.eachLine { String line ->
            VkProfile profile = new VkProfile(jsonSlurper.parseText(line))

            boolean isNotEmptyEducation = false

            profile.educationRecords
                    .collect({ new EducationRecord(it) })
                    .findAll({ it?.university?.country != null && it?.university?.country == UKRAINE })
                    .each { EducationRecord educationRecord ->
                        univVsCountryFile.append "${profile?.city?.vkId?:0},${profile?.country?.vkId?:0},${educationRecord.university?.vkId?:0},${educationRecord.faculty?.vkId?:0},${educationRecord.university?.city?.vkId?:0},${educationRecord.university?.country?.vkId?:0}\n"
                        isNotEmptyEducation = true

                        universities += educationRecord.university
                        faculties += educationRecord.faculty

                        univVsCountrySize++
                    }

            if (isNotEmptyEducation) {
                log.debug "Get at least one education record from profile id:$profile.vkId"

                nonEmptyEducationRecords++

                countries += profile.country
                cities += profile.city
            }

            dataSetSize++
        }

        File universitiesFile = reportFilesService.createEmptyFile("$EXPLORATION_DIRECTORY_PATH/$UNIVERSITIES_FILE_NAME")
        log.info "Generating $universitiesFile.path..."
        universitiesFile.text = 'id,name,city,country\n'
        universities.findAll({ it != null }).sort { it.vkId }.each { University university ->
            universitiesFile.append """${university.vkId},"${university.name}",${university.city?.name},${university.country?.name}\n"""
        }

        File facultiesFile = reportFilesService.createEmptyFile("$EXPLORATION_DIRECTORY_PATH/$FACULTIES_FILE_NAME")
        log.info "Generating $facultiesFile.path..."
        facultiesFile.text = 'id,name,university_id,university_name,city,country\n'
        faculties.findAll({ it != null }).sort { it.vkId }.each { Faculty faculty ->
            facultiesFile.append """${faculty.vkId},"${faculty.name}",${faculty.university?.vkId?:0},"${faculty.university?.name},${faculty.university?.city?.name},${faculty.university?.country?.name}\n"""
        }

        File countriesFile = reportFilesService.createEmptyFile("$EXPLORATION_DIRECTORY_PATH/$COUNTRIES_FILE_NAME")
        log.info "Generating $countriesFile.path..."
        countriesFile.text = 'id,name\n'
        countries.findAll({ it != null }).sort { it.vkId }.each { Country country ->
            countriesFile.append """${country.vkId},"${country.name}"\n"""
        }

        File citiesFile = reportFilesService.createEmptyFile("$EXPLORATION_DIRECTORY_PATH/$CITIES_FILE_NAME")
        log.info "Generating $citiesFile.path..."
        citiesFile.createNewFile()
        citiesFile.text = 'id,name,country_name\n'
        cities.findAll({ it != null }).sort { it.vkId }.each { City city ->
            citiesFile.append """${city.vkId},"${city.name}",${city.country?.vkId?:0},"${city.country?.name}"\n"""
        }

        return new ExploreResponse(timeTaken: TimeUnit.SECONDS.convert(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS), dataSetSize: dataSetSize, nonEmptyEducationRecords: nonEmptyEducationRecords, univVsCountrySize: univVsCountrySize)
    }
}
