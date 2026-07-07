plugins {
    id("org.framefork.build.library-internal")
}

dependencies {
    api(project(":typed-ids-testing"))
    api(testFixtures(project(":typed-ids-hibernate-70")))

    api(libs.hibernate.orm.v70)
    api(libs.hypersistence.utils.hibernate70)

}
