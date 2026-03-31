\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{FedericoIIswedoc}[2025-10-17 Software Engineering Documentation Class]
\RequirePackage{expl3}
%%% Carichiamo la classe base %%%
\LoadClass[a4paper,twoside,11pt]{report}
\RequirePackage{graphicx}
\RequirePackage{geometry}
\RequirePackage{colortbl}
\RequirePackage[bookmarks,pdfusetitle]{hyperref}
\usepackage[T1]{fontenc} % Font encoding for extended character sets.
\usepackage{newpxmath} % Use PX fonts for math (alternative to standard fonts).
\usepackage{fvextra} % Enhanced customisation of verbatim environments.
\usepackage{csquotes} % Context-sensitive quotation marks.
\usepackage{fontspec} % Required for custom fonts with XeLaTeX or LuaLaTeX.
\RequirePackage{array}
\RequirePackage{tabularx}
\usepackage{rotating}
\usepackage{multirow}
\usepackage[normalem]{ulem}
\useunder{\uline}{\ul}{}

%%% Configurazione margini %%%
\geometry{
	paper=a4paper,
	top=2.54cm,
	bottom=2.54cm,
	left=3.15cm,
	right=3.15cm,
	footskip=1.6cm,
	headsep=0.75cm,
	% showframe % Uncomment to display margin frames for debugging.
}

\addtolength{\textheight}{-20pt} % Decrease text height slightly.
\setlength{\headheight}{20pt} % Set header height.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Font %%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\linespread{1.2} % Slightly increase line spacing.

\setmainfont[
Path = ./Fonts/,
UprightFont = Minion-Pro-Display.otf,
BoldFont = Minion-Pro-Bold.ttf,
ItalicFont = Minion-Pro-Italic.ttf,
BoldItalicFont = Minion-Pro-Bold-Italic.ttf,
]{MinionFont}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Document Colors %%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\RequirePackage{xcolor}


% Main document color; used mainly in citation links.
\colorlet{maincolor}{black}

% Color used in the numeration of the chapter style 'classic'.
\colorlet{chapterdarkred}{red!30!black}

% Color used in the numeration of the chapter style 'modern'.
\colorlet{chaptergray}{gray!80!white}

% Color used in the typesetting of the front page.
\definecolor{frontpagedark}{HTML}{1c1c1c}

% Colors used in the custom block environment.
\definecolor{note}{HTML}{0969da}
\definecolor{tip}{HTML}{1a7f37}
\definecolor{warning}{HTML}{9a6700}
\definecolor{todo}{HTML}{d1242f}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Table Of Contents %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\usepackage{titletoc}
\usepackage{tocloft}

\usepackage[italian]{babel}
\addto\captionsitalian{%
	\renewcommand{\contentsname}{\hfill\normalfont\fontsize{28}{22}\selectfont Indice}%
	\renewcommand{\listfigurename}{\hfill\normalfont\fontsize{28}{22}\selectfont Lista delle Figure}%
	\renewcommand{\listtablename}{\hfill\normalfont\fontsize{28}{22}\selectfont Lista delle Tabelle}%
}

\newcommand{\addchapterlike}[1]{%
	\addcontentsline{toc}{chapter}{\normalfont\textit{\textcolor{chapterdarkred}{#1}}}%
}

% Formattazione capitoli nell'indice (rossi, senza puntini)
\renewcommand{\cftchapfont}{\color{chapterdarkred}\bfseries}
\renewcommand{\cftchappagefont}{\color{chapterdarkred}\bfseries}
\renewcommand{\cftchapleader}{\hfill} % Rimuove i puntini
\renewcommand{\cftchappresnum}{\bfseries}
\renewcommand{\cftchapaftersnum}{} 

% Formattazione sezioni (con puntini)
\renewcommand{\cftsecfont}{\normalfont}
\renewcommand{\cftsecpagefont}{\normalfont}
\renewcommand{\cftsecleader}{\cftdotfill{\cftdotsep}}

% Formattazione sottosezioni (con puntini, indentate)
\renewcommand{\cftsubsecfont}{\normalfont}
\renewcommand{\cftsubsecpagefont}{\normalfont}
\renewcommand{\cftsubsecleader}{\cftdotfill{\cftdotsep}}

% Indentazione
\setlength{\cftchapindent}{0em}
\setlength{\cftsecindent}{1.5em}
\setlength{\cftsubsecindent}{3.8em}

% Spaziatura
\setlength{\cftbeforechapskip}{0.8em}
\setlength{\cftbeforesecskip}{0.3em}
\setlength{\cftbeforesubsecskip}{0.2em}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Chapter Style %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\usepackage{titlesec}

% Formato del capitolo
\titleformat{\chapter}[display]
{\normalfont\bfseries\raggedleft} % Allineamento a destra
{\color{chapterdarkred}\fontsize{48}{50}\selectfont\thechapter} % Numero grande e rosso
{0.5em} % Spazio tra numero e titolo
{\fontsize{24}{28}\selectfont} % Titolo grande e nero bold

% Spaziatura prima e dopo il capitolo
\titlespacing*{\chapter}
{0pt} % margine sinistro
{0pt} % spazio prima del capitolo
{2em} % spazio dopo il capitolo

%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Headers and Footers %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\usepackage{fancyhdr}

\pagestyle{fancy}
\fancyhf{} % Pulisce header e footer

% Pagine pari (sinistra) - numero a sinistra
\fancyfoot[LE]{\thepage}

% Pagine dispari (destra) - numero a destra  
\fancyfoot[RO]{\thepage}

% Rimuove le linee di header
\renewcommand{\headrulewidth}{0pt}

% Stile per le pagine dei capitoli (che usano 'plain')
\fancypagestyle{plain}{%
	\fancyhf{}%
	\fancyfoot[LE]{\thepage}%
	\fancyfoot[RO]{\thepage}%
	\renewcommand{\headrulewidth}{0pt}%
}
