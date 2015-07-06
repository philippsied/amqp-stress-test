presentation = slides.tex
documentation = paper.tex

default:
	@echo "Use paper|slides|all|clean"
	@echo "Use"
	@echo "   make paper\tto create the documentation"
	@echo "   make slides\tto create the presentation slides"
	@echo "   make all\tto create both"
	@echo "   make clean\tto remove the tmp files from latex"

all: paper slides

paper:
	pdflatex -interaction=nonstopmode $(documentation)
	pdflatex -interaction=nonstopmode $(documentation)


slides:
	xelatex -interaction=nonstopmode $(presentation)
	xelatex -interaction=nonstopmode $(presentation)


clean:
	rm -f *.aux *.log *.aux *.out *.nav *.snm *.toc




