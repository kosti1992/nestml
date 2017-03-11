# NESTML - The NEST Modelling Language

NESTML is a domain specific language that supports the specification of neuron models
in a precise and concise syntax, based on the syntax of Python. Model equations
can either be given as a simple string of mathematical notation or as an algorithm written
in the built-in procedural language. The equations are analyzed by NESTML to compute
an exact solution if possible or use an appropriate numeric solver otherwise.

This is a non-official branch of the original NESTML repository dealing with the development of model-transformations from NESTML to other 
languages of the neuroscientific domain. For more information regarding the orignal work, please refer to the initial rep: https://github.com/nest/nestml .

The generated Nestml2LEMS framework can be executed by the following command after successfully build:

java -jar nestml.jar Source_Path -lems -simSteps X -units_external -config Artifact_Path

where options: 
Source_Path : Indicates the source dir which contains all models which will be transformed.

lems : Indicates that a LEMS counterpiece shall be generated (and not Nestml). 
					
config: (Optional) Indicates the path to an external artifact which contains additional elements which shall be added to the target model, e.g. handwritten code.									
									
									
Currently supported functions are:
integrate(VAR) : the integration of a variable VAR.
emit_spike(): instructs the component to emit an event/spike.
step(VAR): calculats the amount of steps that can be done during a VAR amount of time. 									

