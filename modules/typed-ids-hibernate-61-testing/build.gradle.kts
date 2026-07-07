plugins {
    id("org.framefork.build.library-internal")
}

dependencies {
    api(project(":typed-ids-testing"))
    api(testFixtures(project(":typed-ids-hibernate-61")))

    api(libs.hibernate.orm.v61)
    api(libs.hypersistence.utils.hibernate61)

}
