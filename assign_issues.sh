#!/bin/bash
# Script to assign KrishiMitra issues to Santoshmshine
# Run this after installing GitHub CLI: brew install gh

echo "Reassigning KrishiMitra issues to @Santoshmshine..."

# List of issues assigned to Santosh
ISSUES="2 3 14 15 16 30 31 32 34 35 38"

# Assign each issue using correct GitHub CLI syntax
for issue in $ISSUES; do
  echo "Assigning issue #$issue to Santoshmshine..."
  gh issue edit $issue --add-assignee Santoshmshine
done

echo "✅ All issues reassigned!"
echo ""
echo "To verify, run: gh issue view <issue_number> --json assignees"