package com.github.vuzoll.explorevk

import groovy.json.JsonSlurper
import org.springframework.web.bind.annotation.*

import java.util.concurrent.TimeUnit

@RestController
class ExploreVkController {

    static String DATA_FILE_PATH = System.getenv('EXPLORE_VK_DATA_FILE_PATH') ?: '/data/vk.data'
    static String EXPLORATION_DIRECTORY_PATH = System.getenv('EXPLORE_VK_EXPLORATION_DIRECTORY_PATH') ?: '/data'
    static String UNIV_VS_COUNTRY_FILE_NAME = System.getenv('EXPLORE_VK_UNIV_VS_COUNTRY_FILE_NAME') ?: 'univ-vs-country.csv'
    static String UNIVERSITIES_FILE_NAME = System.getenv('EXPLORE_VK_UNIVERSITIES_FILE_NAME') ?: 'universities.csv'
    static String FACULTIES_FILE_NAME = System.getenv('EXPLORE_VK_FACULTIES_FILE_NAME') ?: 'faculties.csv'
    static String COUNTRIES_FILE_NAME = System.getenv('EXPLORE_VK_COUNTRIES_FILE_NAME') ?: 'countries.csv'
    static String CITIES_FILE_NAME = System.getenv('EXPLORE_VK_CITIES_FILE_NAME') ?: 'cities.csv'

    JsonSlurper jsonSlurper = new JsonSlurper()
    File dataFile = new File(DATA_FILE_PATH)
    File univVsCountryFile = new File("$EXPLORATION_DIRECTORY_PATH/$UNIV_VS_COUNTRY_FILE_NAME")
    File universitiesFile = new File("$EXPLORATION_DIRECTORY_PATH/$UNIVERSITIES_FILE_NAME")
    File facultiesFile = new File("$EXPLORATION_DIRECTORY_PATH/$FACULTIES_FILE_NAME")
    File countriesFile = new File("$EXPLORATION_DIRECTORY_PATH/$COUNTRIES_FILE_NAME")
    File citiesFile = new File("$EXPLORATION_DIRECTORY_PATH/$CITIES_FILE_NAME")

    @RequestMapping(path = '/explore', method = RequestMethod.POST)
    @ResponseBody ExploreResponse explore() {
        long startTime = System.currentTimeMillis()
        int dataSetSize = 0
        int nonEmptyEducationRecords = 0

        Set<University> universities = []
        Set<Faculty> faculties = []
        Set<Country> countries = []
        Set<City> cities = []

        if (univVsCountryFile.exists()) {
            univVsCountryFile.delete()
        }
        univVsCountryFile.createNewFile()
        univVsCountryFile.text = 'university_id,faculty_id,country_id,city_id\n'
        dataFile.eachLine { String line ->
            VkProfile profile = new VkProfile(jsonSlurper.parseText(line))

            universities += profile.educationRecords.university
            faculties += profile.educationRecords.faculty
            countries += profile.country
            cities += profile.city

            int isNotEmptyEducation = 0

            profile.educationRecords.collect({ new EducationRecord(it) }).each { EducationRecord educationRecord ->
                univVsCountryFile.append "${educationRecord?.university?.vkId?:0},${educationRecord?.faculty?.vkId?:0},${profile?.country?.vkId?:0},${profile?.city?.vkId?:0}\n"
                isNotEmptyEducation = 1
            }

            nonEmptyEducationRecords += isNotEmptyEducation
            dataSetSize++
        }

        if (universitiesFile.exists()) {
            universitiesFile.delete()
        }
        universitiesFile.createNewFile()
        universitiesFile.text = 'id,name\n'
        universities.findAll({ it != null }).collect({ new University(it) }).sort { it.vkId }.each { University university ->
            universitiesFile.append """${university.vkId},"${university.name}"\n"""
        }

        if (facultiesFile.exists()) {
            facultiesFile.delete()
        }
        facultiesFile.createNewFile()
        facultiesFile.text = 'id,name,university_id,university_name\n'
        faculties.findAll({ it != null }).collect({ new Faculty(it) }).sort { it.vkId }.each { Faculty faculty ->
            facultiesFile.append """${faculty.vkId},"${faculty.name}",${faculty.university.vkId},"${faculty.university.name}"\n"""
        }

        if (countriesFile.exists()) {
            countriesFile.delete()
        }
        countriesFile.createNewFile()
        countriesFile.text = 'id,name\n'
        countries.findAll({ it != null }).sort { it.vkId }.each { Country country ->
            countriesFile.append """${country.vkId},"${country.name}"\n"""
        }

        if (citiesFile.exists()) {
            citiesFile.delete()
        }
        citiesFile.createNewFile()
        citiesFile.text = 'id,name,country_id,country_name\n'
        cities.findAll({ it != null }).sort { it.vkId }.each { City city ->
            citiesFile.append """${city.vkId},"${city.name}",${city.country.vkId},"${city.country.name}"\n"""
        }

        return new ExploreResponse(timeTaken: TimeUnit.SECONDS.convert(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS), dataSetSize: dataSetSize, nonEmptyEducationRecords: nonEmptyEducationRecords)
    }
}
