plugins {
    id("framefork.java")
}

dependencies {
    api(project(":typed-ids-testing"))
    api(testFixtures(project(":typed-ids-hibernate-61")))

    api(libs.hibernate.orm.v61)
    api(libs.hypersistence.utils.hibernate61)
}
