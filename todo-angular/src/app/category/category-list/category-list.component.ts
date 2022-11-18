import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';

import { Category } from '../../models/category';
import { CategoryService } from '../../service/category.service';

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {

  categoryList: Category[] = [];

  constructor(
    private location        : Location,
    private categoryService : CategoryService
  ) { }

  ngOnInit(): void {
    this.getCategoryList();
  }

  getCategoryList(): void {
    this.categoryService.getCategoryList().subscribe(
      categoryList => this.categoryList = categoryList
    )
  }

  /*
  add(name: string): void {
    name = name.trim();
    if (!name) { return; }
    this.heroService.addHero({ name } as Hero)
      .subscribe(hero => {
        this.heroes.push(hero);
      });
  }
  */

  delete(category: Category): void {
    this.categoryList = this.categoryList.filter(t => t !== category);
    this.categoryService.deleteCategory(category.id).subscribe();
  }

  goBack(): void {
    this.location.back();
  }
}
