@echo off
:: This script runs a benchmark for a list of StarCraft maps.

:: Define the output file for the benchmark results.
set "output=benchmark_results.csv"

:: Remove the old CSV file if it already exists to start fresh.
if exist "%output%" del "%output%"

:: Write the header row to the CSV file.
echo Map,Win,Score,Killed,Lost,Resources balance,Time > "%output%"

echo Starting benchmark...
echo.


for %%M in (
	"ums/rav/protoss/1Drag_v_1Zeal.scm"
	"ums/rav/protoss/ZealDrag_v_Lings.scm"
	"ums/rav/protoss/ZealDrag_v_LingsHydra.scm"
	"ums/rav/protoss/Drag_v_Hydra.scm"
	"ums/rav/protoss/Zeal_v_Lings.scm"
	"ums/rav/protoss/ZealDrag_v_ZealDrag.scm"
	"ums/rav/protoss/ZealDrag_v_Zeal.scm"
	"ums/rav/protoss/8Drag_v_8Drag.scm"
	"ums/rav/protoss/4+3Zeal_v_4+3Zeal.scm"
) do (
    echo Running benchmark for map: "%%~M"
    java -jar C:/Users/ravae/AppData/Roaming/scbw/bots/AtlantisP/AI/Atlantis.jar --map="%%~M" --benchmark
    echo Completed map: "%%~M"
    echo.
)

echo Benchmark complete. Results saved to %output%

start "" "%output%"

timeout /t 3 /nobreak >nul

::if exist "%output%" del "%output%"

:: Explicitly exit the command prompt window.
exit
