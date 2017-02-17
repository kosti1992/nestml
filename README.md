# NESTML - The NEST Modelling Language

NESTML is a domain specific language that supports the specification of neuron models
in a precise and concise syntax, based on the syntax of Python. Model equations
can either be given as a simple string of mathematical notation or as an algorithm written
in the built-in procedural language. The equations are analyzed by NESTML to compute
an exact solution if possible or use an appropriate numeric solver otherwise.

<<<<<<< HEAD
This is a non-official branch of the original NESTML repository dealing with the development of model-transformations from NESTML to other 
languages of the neuroscientific domain. For more information regarding the orignal work, please refer to the initial rep: https://github.com/nest/nestml .

The generated Nestml2LEMS framework can be executed by the following command after successfully build:

java -jar nestml.jar Source_Path -lems -simSteps X -units_external -config Artifact_Path

where options: 
Source_Path : Indicates the source dir which contains all models which will be transformed.

lems : Indicates that a LEMS counterpiece shall be generated (and not Nestml). 

simSteps X: (Optional) Indicates the length of a single simulation step in the target simulator. This option is required in order to be able
								to determine compute the solution of "steps(..)" function.

units_external: (Optional) Indicates that physical units and the corresponding dimension should be generated to a separate file. This can be 
									convenient if several models are generated, since only one set of units and dimensions is required.
									
config: (Optional) Indicates the path to an external artifact which contains additional elements which shall be added to the target model, e.g. handwritten code.									
									
									
Currently supported functions are:
integrate(VAR) : the integration of a variable VAR.
emit_spike(): instructs the component to emit an event/spike.
step(VAR): calculats the amount of steps that can be done during a VAR amount of time. 									

=======
## Directory structure

`docker` - A docker containers with the complete NESTML software pipeline installed. Once based on the latest release of NESTML. One that builds the latest development version of NESTML.

`models` - Example neuron models in NESTML format

`src` - The source code of NESTML

## Installing and running NESTML

To install NESTML, the following requirements need to be met:
* [Apache Maven](https://maven.apache.org/)
* [Oracle Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [sympy >= 1.0.1dev](https://github.com/sympy/sympy)

Compilation can then be issued by running the following commands
```
cd <nestml_clone>
mvn clean install
```

If everything ran without errors, then the `nestml.jar` artifact will be created in `<nestml_clone>/target subfolder`. Run the `nestml` tool using

```
java -jar nestml.jar <models> --target <build_dir>
```
where `<models>` is a directory containing one or more `.nestml` files and `build_dir` is the directory, into which the C++ are put together with an extension module and the corresponding build infrastructure for NEST.

## Running NESTML using Docker

As NESTML has quite some dependencies, which makes it a bit complicated to install and run it. To lower the burden, we have created a [Docker](https://www.docker.com/) container for you. The `Dockerfile`s and corresponding helper scripts can be found in the `docker` folder. In order to use this method, you have to have Docker installed on your machine. Please refer to the [installation instructions](https://docs.docker.com/engine/installation) or use the packages from your Linux distribution's software manager.

### Provisioning

The container can be provisioned (created) by first changing to the `docker` directory of your clone of the `nestml` Git repository and then running the `nestml_docker.sh` script:

```
cd <nestml_clone>/docker
./nestml_docker.sh provision
```

This will download all required packages and libraries and create a container that uses the pre-built version of the [latest release of NESTML](https://github.com/nest/nestml/releases). If you are interested in using the bleeding edge version of NESTML (a.k.a. Git master), you can add the argument `--dev` to the invocation of the `./nestml_docker.sh` script.

If everything goes well, the list printed by the command 'docker images' should now contain the 'nestml_release' container. If you experience an error, please [open an issue](https://github.com/nest/nestml/issues) so we can look into and fix it.

### Running

To actually convert your model files written in NESTML to NEST C++, you have to run the Docker container. This is again done using the `nestml_docker.sh` script, which for this purpos gets the command `run` as first argument and one or more folders containing one or more `.nestml` files (the folder is called `<models>` in the following description):

```
./nestml_docker.sh run <models>
```

This run creates a subfolder `build` in the `<models>` directory that contains the generated code and all infrastructure and source files for an extension module for NESTML, which can be dynamically loaded. The module will have the same name as the folder in which you stored the `.nestml` files.

As an example, let's consider the folder `<nestml_clone>/models` that contains all models bundled with NESTML. Among others, it contains the files `aeif_cond_alpha.nestml` and `hh_psc_alpha.nestml`. The resulting module will be called `models`.

In order to compile the module and install it into the NEST installation directory, you have to use the following commands:
```
cd <models>/build
cmake -Dwith-nest=<nest_install_dir>/bin/nest-config .
make all
make install
```

Again, if everything goes well, you can now use the generated module in your SLI and PyNEST scripts by using the corresponding version of the `Install` command. For SLI the invocation looks like this: `(<models>) Install`, for PyNEST it reads `nest.Install("<models>")`. After loading the module, the contained models can be instantiated just as the built-in models using the `Create` command in SLI and PyNEST, respectively.

With the previous example, this results in a module called `models`, which can be loaded by NEST and gives access to (among others) the two afforementioned neuron models:

```
nest.Install("models")
aeif_cond_alpha = nest.Create("aeif_cond_alpha_neuron")
hh_psc_alpha = nest.Create("hh_psc_alpha_neuron")
```
>>>>>>> 2e69fd2d206336c55c0ba3f4d9fe79da8544e28f
