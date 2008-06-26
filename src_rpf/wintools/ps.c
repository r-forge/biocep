/*
    ps.c - Despliega la lista de procesos y modulos activos

    Copyright (C) 2001 Luis Carlos Castro Skertchly

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA	


	Inicio: 12 de Diciembre de 2001 (Basado en el programa anterior ps95.c)

	Uso:
	ps [-f]

	Versiones corriendo:
	1.0	- 12 de Diciembre de 2001

*/

#include <stdio.h>
#include <windows.h>
#include <tlhelp32.h>


HANDLE hSnapshot, hProcSnapshot;
PROCESSENTRY32 pe;
MODULEENTRY32 me;
char flag;
char flagdll;

int main(int argc, char **argv)
{

	fprintf(stderr, "PS - Process list\n(C) 2001, Luis C. Castro Skertchly\n\n");

	flagdll = 0;
	if (argc > 1)
	{
		if (!strcmp(argv[1], "-f"))
			flagdll = 1;
	}

	hSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	pe.dwSize = sizeof(pe);
	puts("PID  PPID  THR PR NAME");
	if(!Process32First(hSnapshot, &pe))
	{
		puts("Impossible! There are no process running!\n");
		CloseHandle(hSnapshot);
		return 1;
	}
	
	do
	{
		hProcSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPMODULE, pe.th32ProcessID);
		me.dwSize = sizeof(me);
		flag = Module32First(hProcSnapshot, &me);
		if (!pe.th32ProcessID) flag = 0;
		printf("%4ld %4ld %3ld %3ld %s\n", pe.th32ProcessID, pe.th32ParentProcessID, pe.cntThreads, pe.pcPriClassBase, flag ? me.szExePath : pe.szExeFile);
		if (flag)
		{
			if (flagdll)
			{
				while (Module32Next(hProcSnapshot, &me))
					printf("%17s %s\n", "  ", me.szExePath);				
			}
		}	
	} while (Process32Next(hSnapshot, &pe));
	CloseHandle(hSnapshot);

	return 0;
}
