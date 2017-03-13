# NESTML - The NEST Modeling Language

NESTML is a domain specific language that supports the specification of neuron models
in a precise and concise syntax, based on the syntax of Python. Model equations
can either be given as a simple string of mathematical notation or as an algorithm written
in the built-in procedural language. The equations are analyzed by NESTML to compute
an exact solution if possible or use an appropriate numeric solver otherwise.

This is an official branch of the original NESTML repository dealing with the development of model-transformations from NESTML to other 
languages of the neuroscientific domain. For more information regarding the original work, please refer to the initial rep: https://github.com/nest/nestml .

The generated Nestml2LEMS framework can be executed by the following command after successful build operation:

```
java -jar nestml.jar [source_path] -lems [-config Artifact_Path]
```

with the following options:
 
source_path : Indicates the source directory containing all models which will be transformed to the target modeling language.

-lems : Indicates that a LEMS counter piece shall be generated. In case this option is not used, a NEST model will be generated. 
					
-config: Indicates the path to an external artifact which contains additional elements and simulation specifications, e.g. handwritten code, duration of a simulation step.	An artifact has to be provided as a _.xml_ with following format:

```
<Target specification="[TARGET-LANGUAGE]" name="[TARGET-MODEL-NAME]" [OPTION]="[VALUE]">
	<!--HANDWRITTEN CODE, e.g.-->
	<Attachments name="pulseGeneratorDL" type="pulseGeneratorDL"/>
</Target>	
```								
Here, the _target-language_ option can be used to indicate to which language, consequently the syntax, the encapsulated elements shall be transformed. This options represents an extension points for future work, currently only **LEMS** as option is supported. The _name_ parameter of the artifact is used to specify the target model for which the encapsulated specifications are used. The value of this option has to be the name of a specific target model or a list or such in the following format: _TARGET-NAME1;TARGET-NAME2;...._ ,i.e. individual names separated by a column. The remaining options as contained in the header represent an interface for additional specifications. Currently supported are _units_external_ to indicate that, in the case LEMS has been selected as target language, all physical units have to be generated to a separate file, and _simulation_steps_ which can be used to provide the duration of a single simulation steps as sometimes required for the derivation of explicit values. Inside the _Target_ specification, handwritten code can be placed. The framework currently supports the following LEMS attributes: _Attachment_,_Parameter_,_DerivedParameter_,_EventPort_, _StateVariable_ and _TimeDerivative_ . In the case that a not well-formed artifact has been provided, a corresponding error message is printed to the screen and the artifact is skipped.   									
									
The NESTML2LEMS sub framework supports almost all characteristics of the NestML specification language. For a list of all fully supported source models and corresponding keywords please refer to sub-folder _examples_ . Currently, the concept of _buffers_ is not fully supported due to the lack of expressiveness in the target language LEMS. Additionally, only a small set of function calls has been provided with a semantic preserving transformation.  

Currently supported functions are:
**integrate([VAR])** : the integration of a variable VAR.
**emit_spike()**: instructs the component to emit an event/spike.
**step(VAR)**: calculates the amount of steps that can be done during a VAR amount of time. 									

