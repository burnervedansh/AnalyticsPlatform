@echo off
REM Script to clean Git repository and remove unwanted files

echo ========================================
echo Git Repository Cleanup Script
echo ========================================
echo.
echo This will remove the following from Git:
echo - target/ directories (build artifacts)
echo - node_modules/ (dependencies)
echo - .vscode/ (IDE settings)
echo - Images and PDFs (optional)
echo.
echo WARNING: This will rewrite Git history!
echo Make sure you have a backup if needed.
echo.
pause

echo.
echo [1/5] Removing cached files from Git...
git rm -r --cached .

echo.
echo [2/5] Adding .gitignore...
git add .gitignore

echo.
echo [3/5] Re-adding only the files that should be tracked...
git add .

echo.
echo [4/5] Committing changes...
git commit -m "chore: add .gitignore and remove build artifacts, dependencies, and IDE files"

echo.
echo [5/5] Force pushing to remote (this will rewrite history)...
echo.
echo FINAL WARNING: This will force push to origin/main
echo Press Ctrl+C to cancel, or
pause

git push origin main --force

echo.
echo ========================================
echo Cleanup complete!
echo ========================================
echo.
echo Your repository now only contains source files.
echo Build artifacts and dependencies are excluded.
echo.
pause
