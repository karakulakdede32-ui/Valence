import re
with open('build.gradle', 'r') as f:
    content = f.read()

# Replace the commented out deps with actual deps including JEI
old = """    // Jade & EMI dependencies removed due to maven resolution issues in sandbox
    // implementation fg.deobf("curse.maven:jade-324717:4833182")
    // compileOnly fg.deobf("dev.emi:emi-forge:1.1.13+1.20.1:api")"""

new = """    // JEI, Jade & EMI integration
    compileOnly fg.deobf("mezz.jei:jei-1.20.1-forge-api:15.3.0.8")
    compileOnly fg.deobf("mezz.jei:jei-1.20.1-common-api:15.3.0.8")
    implementation fg.deobf("curse.maven:jade-324717:4833182")
    compileOnly fg.deobf("dev.emi:emi-forge:1.1.13+1.20.1:api")"""

content = content.replace(old, new)
with open('build.gradle', 'w') as f:
    f.write(content)
