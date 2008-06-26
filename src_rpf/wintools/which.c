/*
    which.c - Busca un programa en el PATH

    Copyright (C) 1999 Luis Carlos Castro Skertchly

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

#define _CRT_SECURE_NO_DEPRECATE
#define _WIN32_WINNT 0x0501
#define WIN32_LEAN_AND_MEAN

#include <windows.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>


BYTE szBuffer[256];
BYTE szExt[4];
LPTSTR lpFilePart;

int main(int argc, char **argv)
{
  if (argc < 2)
  {
    printf("%s: usage: which filename\n", argv[0]);
    return 1;
  }

  _strlwr(argv[1]);
  if (!SearchPath(NULL, argv[1], NULL, sizeof(szBuffer), szBuffer, &lpFilePart))
  {
    strcpy(szExt, ".exe");
    if (!SearchPath(NULL, argv[1], szExt, sizeof(szBuffer), szBuffer, &lpFilePart))
    {
      strcpy(szExt, ".com");
      if (!SearchPath(NULL, argv[1], szExt, sizeof(szBuffer), szBuffer, &lpFilePart))
      {
        strcpy(szExt, ".bat");
        if (!SearchPath(NULL, argv[1], szExt, sizeof(szBuffer), szBuffer, &lpFilePart))
        {
          fprintf(stderr, "%s: not found\n", argv[1]);
          return  1;
        }
      }
    }
  }
  printf(szBuffer);
  return 0;
}

