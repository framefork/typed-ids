plugins {
    id("org.framefork.build.library-internal")
}

dependencies {
    api(project(":typed-ids-testing"))
    api(testFixtures(project(":typed-ids-hibernate-63")))

    api(libs.hibernate.orm.v63)
    api(libs.hypersistence.utils.hibernate63)

}
