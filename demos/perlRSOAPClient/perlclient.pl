#!/usr/bin/perl

use SOAP::Lite;
use SOAP::WSDL;
use strict;
use warnings;

my $service = SOAP::Lite -> service('http://192.168.2.100:8080/rws/rGlobalEnvFunction?wsdl');
 
my $session = $service->logOn(SOAP::Data->name("arg0" => ""),SOAP::Data->name("arg1" => "guest"), SOAP::Data->name("arg2" => "guest") 
, SOAP::Data->name("arg3" => "privatename=toto") ) ;
 
$service->consoleSubmit(SOAP::Data->name("arg0" => $session), SOAP::Data->name("arg1" => "x='test2';library(vsn);data(kidney);justvsn(kidney);"));
print "Status: ", $service->getStatus(SOAP::Data->name("arg0" => $session)), "\n";
#$service->consoleSubmit(SOAP::Data->name("arg0" => $session), SOAP::Data->name("arg1" => "setwd('C:/wtest')"));
#print "Status: ", $service->getStatus(SOAP::Data->name("arg0" => $session)), "\n";
#$service->consoleSubmit(SOAP::Data->name("arg0" => $session), SOAP::Data->name("arg1" => "save(x,file='tata')"));
#print "Status: ", $service->getStatus(SOAP::Data->name("arg0" => $session)), "\n";

my $namedarg= SOAP::Data->name('arg2' => \SOAP::Data->value( SOAP::Data->name('name' => 'sep'), SOAP::Data->name('robject' => '***') )) 
->attr( { 'xsi:type' => 'ns1:rNamedArgument' , 'xmlns' => '', 'xmlns:ns1'=>'http://rGlobalEnv.packages.bioconductor.org/'} );


my $p=$service->callAndConvert(SOAP::Data->name("arg0" => $session), SOAP::Data->name("arg1" => "paste") , SOAP::Data->name("arg2" => "aaaa"), SOAP::Data->name("arg2" => "bbb"), $namedarg );
print "paste result: ", $p;

$service->logOff(SOAP::Data->name("arg0" => $session));
