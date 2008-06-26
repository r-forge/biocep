/*
    kill.c - Termina el proceso indicado en la linea de comandos

    Copyright (C) 1999-2002 Luis C. Castro Skertchly

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
*/

#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <string.h>

HANDLE hProcess;
int i;
int pid;

int main(int argc, char **argv)
{

  if (argc < 2)
  {
    fprintf(stderr, "KILL - Kills a process.\nUsage: kill pid ...");
    return 1;
  }

  for(i = 1; i < argc; i++)
  {
    pid = atoi(argv[i]);
    hProcess = OpenProcess(PROCESS_ALL_ACCESS, FALSE, pid);
    if (!hProcess)
    {
      PutError("kill", argv[i]);
      return 1;
    }
    TerminateProcess(hProcess, 0);
  }
  return 0;
}

