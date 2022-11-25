import { Component, createPlatform, OnInit } from '@angular/core';
import { Location } from '@angular/common';

import { Category } from '../../models/category';
import { CategoryColor } from 'src/app/models/color';
import { CategoryService } from '../../service/category.service';

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {

  categoryList: Category[] = [];
  colorList: CategoryColor[] = [];

  constructor(
    private location        : Location,
    public categoryService : CategoryService
  ) { }

  ngOnInit(): void {
    this.getCategoryList();
    this.getColorList();
  }

  getCategoryList(): void {
    this.categoryService.getCategoryList().subscribe(
      categoryList => this.categoryList = categoryList.filter(_ => _.id != 6)
    )
  }

  getColorList(): void {
    this.categoryService.getCategoryColorList().subscribe(
      colorList => this.colorList = colorList
    )
  }

  getColorCode(id: number): string {
    var colorCode = "#eee";
    let color = this.colorList.find(_ => _.id == id);
    if(color) colorCode = color.colorCode;

    return colorCode;
  }

  delete(category: Category): void {
    this.categoryList = this.categoryList.filter(t => t !== category);
    this.categoryService.deleteCategory(category.id).subscribe();
  }

  goBack(): void {
    this.location.back();
  }
}
