# NESTML - The NEST Modeling Language

NESTML is a domain specific language that supports the specification of neuron models
in a precise and concise syntax, based on the syntax of Python. Model equations
can either be given as a simple string of mathematical notation or as an algorithm written
in the built-in procedural language. The equations are analyzed by NESTML to compute
an exact solution if possible or use an appropriate numeric solver otherwise.

This is an official branch of the original NESTML repository dealing with the development of model transformations from NESTML to other languages of the neuroscientific domain. For more information regarding the original work, please refer to the initial repository: https://github.com/nest/nestml .

## Directory structure

Additionally to the structure of the original work, the following directories have been added to enable the transformation:

`src\main\java\org\nest\codegeneration\helpers\LEMSElements` - A set of classes representing individual elements of the target modeling language LEMS. This classes can be used to extend the functionality by modifying specific subroutines of the transformation.

`src\main\java\org\nest\codegeneration\helpers\Expressions` - A set of classes which facilitates processing and manipulation of (mathematical) expressions.

`src\main\resources\org\nest\lems` - All templates used to print the transformed, internal representation of the model.

## Installing and running NESTML and NESTML2LEMS

The same as in the original work (see https://github.com/nest/).

## Usage of the NESTML2LEMS framework

The Nestml2LEMS framework can be executed by the following command after the jar file of the NESTML framework has been built:

```
java -jar nestml.jar [source_path] -lems [-config Artifact_Path]
```

with the following options:
 
source_path : Indicates the source directory containing all models which will be transformed into the target modeling language.

lems : Indicates that a LEMS counter piece shall be generated. In the case this option is not used, a NEST model will be generated. 
                    
config: Indicates the path to an external artifact which contains additional elements and simulation specifications, e.g. handwritten code or the duration of a simulation step. An artifact has to be provided as a _.xml_ with the following format:

```
<Target name="[TARGET-MODEL-NAME]" [OPTION]="[VALUE]">
    <!--HANDWRITTEN CODE, e.g.-->
    <Attachments name="pulseGeneratorDL" type="pulseGeneratorDL"/>
</Target>    
```                                
The _name_ parameter of the artifact is used to specify the target model for which the encapsulated specifications are used. The mValue of this option has to be the name of a specific target model or a list of such in the following format: _TARGET-NAME1;TARGET-NAME2;...._ , i.e. individual names separated by a colon. The remaining options as contained in the header represent an interface for additional specifications. Currently supported are _units_external_ to indicate that, all physical units have to be generated to a separate file, and _simulation_steps_ which can be used to provide the duration of a single simulation step as sometimes required for the derivation of explicit values. Inside the _Target_ specification, a handwritten code can be placed. The framework currently supports the following LEMS elements: _Attachment_, _Parameter_, _DerivedParameter_, _EventPort_, _StateVariable_ and _TimeDerivative_. In the case that a not well-formed artifact has been provided, a corresponding error message is printed to the screen and the artifact is skipped.
                                    
The NESTML2LEMS framework supports almost all characteristics of the NestML specification language. For a list of all fully supported source models and corresponding keywords please refer to sub-folder _examples_. Currently, the concept of _buffers_ is supported by means of synapses attached to the target model. Additionally, only a small set of function calls has been provided with a semantic preserving transformation. User-defined functions are part of the future work.

Currently, supported functions are:

- **integrate(VAR)** : the integration of a variable VAR.
- **integrate_odes()** : instructs the simulator to integrate all ODEs. 
- **emit_spike()**: instructs the component to emit an event/spike.
- **step(VAR)**: calculates the number of steps that can be done during a VAR amount of time. This function call is replaced by an explicitly derived mValue during the transformation process.

The framework supports the following mathematical functions:

- **exp(_X_)** : exponential function of _X_
- **log(_X_)** : logarithm of _X_ to base 10 
- **ln(_X_)** : natural logarithm of _X_
- **sin(_X_),cos(_X_),tan(_X_)** : trigonometric functions
- **sinh(_X_),cosh(_X_),tanh(_X_)** : hyperbolic functions 
- **sqrt(_X_)** : square root of _X_
- **ceil(_X_)** : the ceil function
- **random(_X_)** : the random function with results in the interval [0;_X_]
- **factorial(_X_)** : the factorial function of _X_
- **abs(_X_)** : the absolute mValue of _X_

And the following global constants:

- **e**: Euler's number
- **t**: the global time constant, time past since the start of simulation

A predefined simulation environment and a set of common components, e.g. current generators, is located in the subfolder:  

`misc\sampleSimEnvironment` 

Currently, the following models (contained in the lemsmodels folder) are supported:
-iaf\_cond\_alpha
-iaf\_cond\_exp
-aeif\_cond\_alpha
-aeif_cond_exp
-iaf\_chxk\_2008 
-iaf\_cond\_beta
-iaf\_cond\_exp\_sfa\_rr
-izhikevich\_neuron
-izhikevich\_psc\_alpha




For more information, please refer to the following sources:

* [LEMS homepage](http://lems.github.io/LEMS)
* [LEMS research article](http://journal.frontiersin.org/article/10.3389/fninf.2014.00079/full)
* [NESTML github](https://github.com/nest/nestml)
* [NESTML research article](http://www.nest-initiative.org/publications/Plotnikov2016.pdf)
