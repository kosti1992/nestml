                    <#--!!!!!!This template is currently not used!!!!!!-->
<#--This template generates predefined components utilized by the transformation.-->
<#--@author perun -->
<#--Modified Version of example1.xml as provided by http://lems.github.io/LEMS/-->
<!-- Generated on ${.now}.-->

<Lems>
    <!--------------------------------------------------------------------------------------->
    <!-- Following components are used to generate networks of cells and assisting sources.-->
    <!--------------------------------------------------------------------------------------->
    <!--A type declaration for networks of neurons.-->
    <ComponentType name="Network">
        <Children name="populations" type="Population"/>
        <Children name="connectivities" type="EventConnectivity"/>
        <Children name="ex_connectivities" type="PortToPortConnection"/>
        <Children name="explicitInput" type="explicitInput"/>
    </ComponentType>

    <!--Defines the population of a network.-->
    <ComponentType name="Population">
        <ComponentReference name="component" type="Component"/>
        <Parameter name="size" dimension="none"/>
        <Structure>
            <MultiInstantiate number="size" component="component"/>
        </Structure>
    </ComponentType>

    <#--Connects a generator to a value of a model.-->
    <ComponentType name="explicitInput">
        <ComponentReference name="input" type="basePointCurrentDL"/>
        <Path name="target"/>
        <Text name="destination"/>
        <Text name="sourcePort"/>
        <Text name="targetPort"/>

        <Structure>
            <With instance="target" as="a"/>
            <With instance="target" as="b"/>
            <EventConnection from="a" to="b" receiver="input" receiverContainer="destination"/>
        </Structure>
    </ComponentType>

    <#--Defines the connection types between neurons.-->
    <ComponentType name="EventConnectivity">
        <Link name="source" type="Population"/>
        <Link name="target" type="Population"/>
        <Child name="Connections" type="ConnectionPattern"/>
    </ComponentType>

    <#--A super component for all connection patterns.-->
    <ComponentType name="ConnectionPattern"/>

    <!--Connects all neurons to all other neurons.-->
    <ComponentType name="AllAll" extends="ConnectionPattern">
        <Structure>
            <ForEach instances="../source" as="a">
                <ForEach instances="../target" as="b">
                    <EventConnection from="a" to="b"/>
                </ForEach>
            </ForEach>
        </Structure>
    </ComponentType>

    <#--This component defines a port to port connector.-->
    <ComponentType name="PortToPortConnection">
        <Path name="from"/>
        <Path name="to"/>
        <Path name="sourceport"/>
        <Path name="targetport"/>
        <Structure>
            <With instance="from" as="a"/>
            <With instance="to" as="b"/>
            <EventConnection from="a" to="b" sourcePort="sourceport" targetPort="targetport"/>
        </Structure>
    </ComponentType>

    <!--------------------------------------------------------------------------------------->
    <!-- Following components are used to generate a simulation of environment.-------------->
    <!--------------------------------------------------------------------------------------->
    <#--Used to display the plot-->
    <ComponentType name="Display">
        <Text name="title"/>
        <Parameter name="xmin" dimension="none"/>
        <Parameter name="xmax" dimension="none"/>
        <Parameter name="ymin" dimension="none"/>
        <Parameter name="ymax" dimension="none"/>

        <Parameter name="timeScale" dimension="time"/>
        <Children name="lines" type="Line"/>

        <Simulation>
            <DataDisplay title="title" dataRegion="xmin,xmax,ymin,ymax"/>
        </Simulation>
    </ComponentType>

    <ComponentType name="Line">
        <Parameter name="scale" dimension="*"/>
        <Parameter name="timeScale" dimension="*"/>
        <Text name="color"/>
        <Path name="quantity"/>
        <Simulation>
            <Record quantity="quantity" timeScale="timeScale" scale="scale" color="color"/>
        </Simulation>
    </ComponentType>

    <ComponentType name="OutputFile">
        <Text name="path"/>
        <Text name="fileName"/>

        <Children name="outputColumn" type="OutputColumn"/>

        <Simulation>
            <DataWriter path="path" fileName="fileName"/>
        </Simulation>

    </ComponentType>

    <ComponentType name="OutputColumn">
        <Path name="quantity"/>
        <Simulation>
            <Record quantity="quantity"/>
        </Simulation>
    </ComponentType>

    <ComponentType name="Simulation">
        <Parameter name="length" dimension="time"/>
        <Parameter name="step" dimension="time"/>

        <ComponentReference name="target" type="Component"/>


        <Children name="displays" type="Display"/>

        <Children name="outputs" type="OutputFile"/>

        <Dynamics>
            <StateVariable name="t" dimension="time"/>
        </Dynamics>

        <Simulation>
            <Run component="target" variable="t" increment="step" total="length"/>
        </Simulation>
    </ComponentType>

    <!--------------------------------------------------------------------------------------->
    <!-- Following components are addtional components utilized during the simulation.------->
    <!--------------------------------------------------------------------------------------->

    <#--In all cases required by LEMS models in order to utilize the spike generator.-->
    <Dimension name="time" t="1"/>
    <Unit symbol="ms" dimension="time" power="-3"/>
    <#--This component defines a spike generator which generates spikes each x-th ms.-->
    <#--The time between spikes is handed over as argumenet-->
    <ComponentType name="spikeGenerator">
        <Parameter name="period" dimension="time"/>
        <EventPort name="a" direction="out"/>
        <Exposure name="tsince" dimension="time"/>
        <Dynamics>
            <StateVariable name="tsince" exposure="tsince" dimension="time"/>
            <TimeDerivative variable="tsince" value="1"/>
            <OnCondition test="tsince .gt. period">
                <StateAssignment variable="tsince" value="0"/>
                <EventOut port="a"/>
            </OnCondition>
        </Dynamics>
    </ComponentType>

    <#--A base type for all current generatores which emit a dimensionless value.-->
    <ComponentType name="basePointCurrentDL">
        <Exposure name="I" dimension="none"
                  description="The total (time varying) current produced by this ComponentType"/>
    </ComponentType>

    <#--Generates a pulse after ${delaty} ms of length ${duration} with amplitute of ${amplitude}-->
    <ComponentType name="pulseGeneratorDL" extends="basePointCurrentDL">
        <Parameter name="delay" dimension="time"
                   description="Delay before change in current. Current is zero  prior to this."/>
        <Parameter name="duration" dimension="time"
                   description="Duration for holding current at amplitude. Current is zero after delay + duration."/>
        <Parameter name="amplitude" dimension="none" description="Amplitude of current pulse"/>

    <#--Required by explicitInput, however, events are not passed.-->
        <EventPort name="in" direction="in"/>

        <Dynamics>
            <StateVariable name="I" exposure="I" dimension="none"/>
            <OnCondition test="t .lt. delay">
                <StateAssignment variable="I" value="0"/>
            </OnCondition>

            <OnCondition test="t .geq. delay .and. t .lt. duration + delay">
                <StateAssignment variable="I" value="amplitude"/>
            </OnCondition>

            <OnCondition test="t .geq. duration + delay">
                <StateAssignment variable="I" value="0"/>
            </OnCondition>
        </Dynamics>
    </ComponentType>

    <#--Generates a constant current source with ${amplitude}.-->
    <ComponentType name="currentGeneratorDL" extends="basePointCurrentDL">
        <Parameter name="amplitude" dimension="none" description="The amplitude of the transmitted current."/>
        <EventPort name="in" direction="in"/>
        <Dynamics>
            <StateVariable name="I" exposure="I" dimension="none"/>
            <OnStart>
                <StateAssignment variable="I" value="amplitude"/>
            </OnStart>
        </Dynamics>
    </ComponentType>

    <#--Generates a current generator which evolves over the time by adding a constant value "delta" to the output.-->
    <ComponentType name="linearEvolvingGeneratorDL" extends="basePointCurrentDL">
        <Parameter name="offset" dimension="time" description="The duration of time after which the component start to evolve."/>
        <Parameter name="duration" dimension="time" description="The duration of the emission."/>
        <Parameter name="delta" dimension="none" description="The value added to the current value in each step."/>
        <EventPort name="in" direction="in"/><!--required by LEMS in order to establish connections.-->
        <Constant name="CON1ms" dimension="time" value="1ms"/>
        <Dynamics>
            <StateVariable name="I" exposure="I" dimension="none"/>
            <StateVariable name="tsinceStart" dimension="time"/>
            <StateVariable name="activator" dimension="none"/>
            <TimeDerivative variable="tsinceStart" value="1"/>
            <TimeDerivative variable="I" value="activator*(delta)/CON1ms"/>
            <OnStart>
                <StateAssignment variable="activator" value="0"/>
            </OnStart>
            <OnCondition test="tsinceStart.geq.offset">
                <StateAssignment variable="activator" value="1"/>
            </OnCondition>
            <OnCondition test="tsinceStart.geq.(offset+duration)">
                <StateAssignment variable="activator" value="0"/>
                <StateAssignment variable="I" value="0"/>
            </OnCondition>
        </Dynamics>
    </ComponentType>

</Lems>